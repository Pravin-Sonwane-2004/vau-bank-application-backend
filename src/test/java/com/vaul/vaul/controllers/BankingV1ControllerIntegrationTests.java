package com.vaul.vaul.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.account.AccountType;
import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.repositories.AccountClosureRequestRepository;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.IdempotencyRecordRepository;
import com.vaul.vaul.repositories.KycCaseRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingV1ControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private KycCaseRepository kycCaseRepository;

    @Autowired
    private AccountClosureRequestRepository accountClosureRequestRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRecordRepository;

    @BeforeEach
    void cleanDatabase() {
        idempotencyRecordRepository.deleteAll();
        accountClosureRequestRepository.deleteAll();
        kycCaseRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void createCustomerShouldBeIdempotent() throws Exception {
        String requestBody = """
                {
                  "name": "Alice Customer",
                  "email": "alice@example.com",
                  "password": "secret",
                  "phone": 9999999999
                }
                """;

        String firstResponse = mockMvc.perform(post("/api/v1/customers")
                        .header("Idempotency-Key", "customer-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created successfully"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/customers")
                        .header("Idempotency-Key", "customer-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode firstJson = objectMapper.readTree(firstResponse);
        JsonNode secondJson = objectMapper.readTree(secondResponse);

        assertThat(firstJson.get("id").asLong()).isEqualTo(secondJson.get("id").asLong());
        assertThat(userRepo.count()).isEqualTo(1);
    }

    @Test
    void kycEndpointsShouldSubmitAndDecideCase() throws Exception {
        User customer = saveUser("kyc-user@example.com");

        String submitResponse = mockMvc.perform(post("/api/v1/kyc/cases")
                        .header("Idempotency-Key", "kyc-submit-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "documentType": "AADHAR",
                                  "documentNumber": "1234-5678-9012",
                                  "addressLine": "Pune, Maharashtra"
                                }
                                """.formatted(customer.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long caseId = objectMapper.readTree(submitResponse).get("id").asLong();

        mockMvc.perform(patch("/api/v1/kyc/cases/{id}/decision", caseId)
                        .header("Idempotency-Key", "kyc-decision-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVED",
                                  "reviewNotes": "Verified against submitted document"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.message").value("KYC case approved successfully"));
    }

    @Test
    void openAccountShouldBeIdempotentAndBalanceEndpointShouldExposeSameAccount() throws Exception {
        User customer = saveUser("account-user@example.com");

        String requestBody = """
                {
                  "customerId": %d,
                  "accountType": "SAVINGS",
                  "initialDeposit": 1500.00,
                  "branch": "AMBAD"
                }
                """.formatted(customer.getId());

        String firstResponse = mockMvc.perform(post("/api/v1/accounts")
                        .header("Idempotency-Key", "account-open-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Account opened successfully"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/accounts")
                        .header("Idempotency-Key", "account-open-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long accountId = objectMapper.readTree(firstResponse).get("accountId").asLong();
        Long duplicateAccountId = objectMapper.readTree(secondResponse).get("accountId").asLong();

        assertThat(accountId).isEqualTo(duplicateAccountId);
        assertThat(accountRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/v1/accounts/{id}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    void withdrawalShouldReturn422WhenFundsAreInsufficient() throws Exception {
        User customer = saveUser("withdraw-user@example.com");
        Account account = saveAccount(customer, "202600010001", new BigDecimal("200.00"));

        mockMvc.perform(post("/api/v1/withdrawals")
                        .header("Idempotency-Key", "withdraw-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": %d,
                                  "amount": 500.00
                                }
                                """.formatted(account.getId())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void transferAndStatementEndpointsShouldExposeRecordedTransaction() throws Exception {
        User customer = saveUser("transfer-user@example.com");
        Account fromAccount = saveAccount(customer, "202600020001", new BigDecimal("5000.00"));
        Account toAccount = saveAccount(customer, "202600020002", new BigDecimal("1000.00"));

        String transferResponse = mockMvc.perform(post("/api/v1/transfers")
                        .header("Idempotency-Key", "transfer-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": %d,
                                  "toAccountId": %d,
                                  "amount": 750.00
                                }
                                """.formatted(fromAccount.getId(), toAccount.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long transactionId = objectMapper.readTree(transferResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.amount").value(750.00));

        mockMvc.perform(get("/api/v1/accounts/{id}/statements", fromAccount.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(fromAccount.getId()))
                .andExpect(jsonPath("$.transactions[0].id").value(transactionId));
    }

    @Test
    void closureRequestShouldRejectAccountsWithNonZeroBalance() throws Exception {
        User customer = saveUser("closure-user@example.com");
        Account account = saveAccount(customer, "202600030001", new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/accounts/{id}/closure-requests", account.getId())
                        .header("Idempotency-Key", "closure-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Customer requested account closure"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Account balance must be zero before closure can be requested"));
    }

    private User saveUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setPhone(9999999999L);
        return userRepo.save(user);
    }

    private Account saveAccount(User user, String accountNumber, BigDecimal balance) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(balance);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBranchCode(ExistsBranches.AMBAD.getBranchCode());
        account.setOpenedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }
}

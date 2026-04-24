package com.vaul.vaul.services;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.Transaction;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.account.AccountType;
import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.enums.transaction.TransactionType;
import com.vaul.vaul.repositories.AccountClosureRequestRepository;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.IdempotencyRecordRepository;
import com.vaul.vaul.repositories.KycCaseRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepo;
import com.vaul.vaul.services.interfaces.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountServiceIntegrationTests {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AccountClosureRequestRepository accountClosureRequestRepository;

    @Autowired
    private KycCaseRepository kycCaseRepository;

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
    void transferShouldMoveMoneyAndCreateTransactionRecord() {
        User user = saveUser("transfer-user@example.com");
        Account fromAccount = saveAccount(user, "202600000001", new BigDecimal("5000.00"));
        Account toAccount = saveAccount(user, "202600000002", new BigDecimal("1200.00"));

        TransactionResponseDto response = accountService.transfer(
                new TransferRequestDto(fromAccount.getId(), toAccount.getId(), new BigDecimal("750.00"))
        );

        Account updatedFromAccount = accountRepository.findById(fromAccount.getId()).orElseThrow();
        Account updatedToAccount = accountRepository.findById(toAccount.getId()).orElseThrow();
        List<Transaction> transactions = transactionRepository.findAll();

        assertThat(response.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(response.getMessage()).isEqualTo("Money transferred successfully");
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("4250.00");
        assertThat(response.getDestinationBalanceAfter()).isEqualByComparingTo("1950.00");
        assertThat(updatedFromAccount.getBalance()).isEqualByComparingTo("4250.00");
        assertThat(updatedToAccount.getBalance()).isEqualByComparingTo("1950.00");
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getDescription()).isEqualTo("Money transferred successfully");
    }

    @Test
    void openAccountShouldRejectCurrentAccountBelowMinimumDeposit() {
        User user = saveUser("current-user@example.com");
        AccountOpenRequestDto request = new AccountOpenRequestDto(
                user.getId(),
                AccountType.CURRENT,
                new BigDecimal("1000.00"),
                ExistsBranches.AMBAD
        );

        assertThatThrownBy(() -> accountService.openAccount(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode().value()).isEqualTo(400);
                    assertThat(responseStatusException.getReason()).contains("minimum opening deposit");
                });
    }

    private User saveUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("secret");
        user.setPhone(9999999999L);
        user.setImage(null);
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

package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.accountdtos.*;
import com.vaul.vaul.dtos.customerdtos.CreateCustomerRequestDto;
import com.vaul.vaul.dtos.customerdtos.CustomerResponseDto;
import com.vaul.vaul.dtos.kycdtos.CreateKycCaseRequestDto;
import com.vaul.vaul.dtos.kycdtos.KycCaseResponseDto;
import com.vaul.vaul.dtos.kycdtos.KycDecisionRequestDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.AccountClosureRequest;
import com.vaul.vaul.entities.KycCase;
import com.vaul.vaul.entities.Transaction;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.account.ClosureRequestStatus;
import com.vaul.vaul.enums.kyc.KycCaseStatus;
import com.vaul.vaul.repositories.AccountClosureRequestRepository;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.KycCaseRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepo;
import com.vaul.vaul.services.interfaces.AccountService;
import com.vaul.vaul.services.interfaces.BankingV1Service;
import com.vaul.vaul.services.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BankingV1ServiceImpl implements BankingV1Service {

    private final UserService userService;
    private final UserRepo userRepo;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KycCaseRepository kycCaseRepository;
    private final AccountClosureRequestRepository accountClosureRequestRepository;
    private final IdempotencyService idempotencyService;

    public BankingV1ServiceImpl(
            UserService userService,
            UserRepo userRepo,
            AccountService accountService,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            KycCaseRepository kycCaseRepository,
            AccountClosureRequestRepository accountClosureRequestRepository,
            IdempotencyService idempotencyService
    ) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kycCaseRepository = kycCaseRepository;
        this.accountClosureRequestRepository = accountClosureRequestRepository;
        this.idempotencyService = idempotencyService;
    }

    @Override
    @Transactional
    public CustomerResponseDto createCustomer(String idempotencyKey, CreateCustomerRequestDto requestDto) {
        return idempotencyService.execute(
                "customers:create",
                idempotencyKey,
                requestDto,
                CustomerResponseDto.class,
                () -> {
                    var registeredCustomer = userService.registerUser(toRegisterRequest(requestDto));
                    User savedCustomer = findUserById(registeredCustomer.getId());
                    return toCustomerResponse(savedCustomer, "Customer created successfully");
                }
        );
    }

    @Override
    @Transactional
    public KycCaseResponseDto submitKycCase(String idempotencyKey, CreateKycCaseRequestDto requestDto) {
        return idempotencyService.execute(
                "kyc-cases:create",
                idempotencyKey,
                requestDto,
                KycCaseResponseDto.class,
                () -> {
                    User customer = findUserById(requestDto.getCustomerId());

                    KycCase kycCase = new KycCase();
                    kycCase.setUser(customer);
                    kycCase.setDocumentType(requestDto.getDocumentType().trim());
                    kycCase.setDocumentNumber(requestDto.getDocumentNumber().trim());
                    kycCase.setAddressLine(requestDto.getAddressLine().trim());
                    kycCase.setStatus(KycCaseStatus.PENDING);

                    return toKycResponse(
                            kycCaseRepository.save(kycCase),
                            "KYC case submitted successfully"
                    );
                }
        );
    }

    @Override
    @Transactional
    public KycCaseResponseDto decideKycCase(String idempotencyKey, Long caseId, KycDecisionRequestDto requestDto) {
        return idempotencyService.execute(
                "kyc-cases:decision:" + caseId,
                idempotencyKey,
                requestDto,
                KycCaseResponseDto.class,
                () -> {
                    KycCase kycCase = findKycCaseById(caseId);
                    validateDecisionRequest(requestDto, kycCase);

                    kycCase.setStatus(requestDto.getDecision());
                    kycCase.setReviewNotes(trimToNull(requestDto.getReviewNotes()));
                    kycCase.setDecidedAt(LocalDateTime.now());

                    String message = requestDto.getDecision() == KycCaseStatus.APPROVED
                            ? "KYC case approved successfully"
                            : "KYC case rejected successfully";

                    return toKycResponse(kycCaseRepository.save(kycCase), message);
                }
        );
    }

    @Override
    @Transactional
    public AccountResponseDto openAccount(String idempotencyKey, CreateAccountRequestDto requestDto) {
        return idempotencyService.execute(
                "accounts:create",
                idempotencyKey,
                requestDto,
                AccountResponseDto.class,
                () -> accountService.openAccount(new AccountOpenRequestDto(
                        requestDto.getCustomerId(),
                        requestDto.getAccountType(),
                        requestDto.getInitialDeposit(),
                        requestDto.getBranch()
                ))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponseDto getBalance(Long accountId) {
        return accountService.getBalance(accountId);
    }

    @Override
    @Transactional
    public AccountResponseDto deposit(String idempotencyKey, DepositRequestDto requestDto) {
        return idempotencyService.execute(
                "deposits:create",
                idempotencyKey,
                requestDto,
                AccountResponseDto.class,
                () -> accountService.deposit(requestDto)
        );
    }

    @Override
    @Transactional
    public AccountResponseDto withdraw(String idempotencyKey, WithdrawRequestDto requestDto) {
        return idempotencyService.execute(
                "withdrawals:create",
                idempotencyKey,
                requestDto,
                AccountResponseDto.class,
                () -> {
                    try {
                        return accountService.withdraw(requestDto);
                    } catch (ResponseStatusException exception) {
                        throw remapWithdrawException(exception);
                    }
                }
        );
    }

    @Override
    @Transactional
    public TransactionResponseDto transfer(String idempotencyKey, TransferRequestDto requestDto) {
        return idempotencyService.execute(
                "transfers:create",
                idempotencyKey,
                requestDto,
                TransactionResponseDto.class,
                () -> {
                    try {
                        return accountService.transfer(requestDto);
                    } catch (ResponseStatusException exception) {
                        throw remapTransferException(exception);
                    }
                }
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponseDto getTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Transaction not found with id: " + transactionId
                ));
        return toTransactionResponse(transaction, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatementResponseDto getStatement(Long accountId, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before or equal to to");
        }

        Account account = findAccountById(accountId);
        List<Transaction> transactions = transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);

        List<TransactionResponseDto> statementTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (isWithinRange(transaction.getTimestamp(), from, to)) {
                statementTransactions.add(toTransactionResponse(transaction, null));
            }
        }

        return new AccountStatementResponseDto(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                from,
                to,
                LocalDateTime.now(),
                statementTransactions
        );
    }

    @Override
    @Transactional
    public AccountClosureResponseDto requestAccountClosure(
            String idempotencyKey,
            Long accountId,
            AccountClosureRequestCreateDto requestDto
    ) {
        return idempotencyService.execute(
                "accounts:closure-request:" + accountId,
                idempotencyKey,
                requestDto == null ? new AccountClosureRequestCreateDto() : requestDto,
                AccountClosureResponseDto.class,
                () -> {
                    Account account = findAccountById(accountId);
                    ensureAccountCanBeClosed(account);

                    if (accountClosureRequestRepository.existsByAccountIdAndStatus(
                            accountId,
                            ClosureRequestStatus.REQUESTED
                    )) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Closure request is already pending for account " + accountId
                        );
                    }

                    AccountClosureRequest closureRequest = new AccountClosureRequest();
                    closureRequest.setAccount(account);
                    closureRequest.setStatus(ClosureRequestStatus.REQUESTED);
                    closureRequest.setReason(trimToNull(requestDto == null ? null : requestDto.getReason()));

                    return toClosureResponse(
                            accountClosureRequestRepository.save(closureRequest),
                            "Account closure requested successfully"
                    );
                }
        );
    }

    private registerRequestDto toRegisterRequest(CreateCustomerRequestDto requestDto) {
        return new registerRequestDto(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword(),
                requestDto.getPhone(),
                requestDto.getImage()
        );
    }

    private CustomerResponseDto toCustomerResponse(User user, String message) {
        return new CustomerResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getImage(),
                user.getCreatedAt(),
                message
        );
    }

    private KycCaseResponseDto toKycResponse(KycCase kycCase, String message) {
        return new KycCaseResponseDto(
                kycCase.getId(),
                kycCase.getUser().getId(),
                kycCase.getStatus(),
                kycCase.getDocumentType(),
                kycCase.getDocumentNumber(),
                kycCase.getAddressLine(),
                kycCase.getReviewNotes(),
                kycCase.getSubmittedAt(),
                kycCase.getDecidedAt(),
                message
        );
    }

    private AccountClosureResponseDto toClosureResponse(AccountClosureRequest closureRequest, String message) {
        return new AccountClosureResponseDto(
                closureRequest.getId(),
                closureRequest.getAccount().getId(),
                closureRequest.getStatus(),
                closureRequest.getReason(),
                closureRequest.getRequestedAt(),
                message
        );
    }

    private TransactionResponseDto toTransactionResponse(Transaction transaction, String message) {
        TransactionResponseDto responseDto = new TransactionResponseDto();
        responseDto.setId(transaction.getId());
        responseDto.setType(transaction.getType());
        responseDto.setAmount(transaction.getAmount());
        responseDto.setFromAccountId(transaction.getFromAccountId());
        responseDto.setToAccountId(transaction.getToAccountId());
        responseDto.setBalanceAfter(transaction.getBalanceAfter());
        responseDto.setDestinationBalanceAfter(transaction.getDestinationBalanceAfter());
        responseDto.setTimestamp(transaction.getTimestamp());
        responseDto.setDescription(transaction.getDescription());
        responseDto.setMessage(message);
        return responseDto;
    }

    private void validateDecisionRequest(KycDecisionRequestDto requestDto, KycCase kycCase) {
        if (requestDto.getDecision() == KycCaseStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED");
        }

        if (kycCase.getStatus() != KycCaseStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "KYC case " + kycCase.getId() + " has already been decided"
            );
        }
    }

    private void ensureAccountCanBeClosed(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Only active accounts can request closure"
            );
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Account balance must be zero before closure can be requested"
            );
        }
    }

    private boolean isWithinRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        if (from != null && timestamp.isBefore(from)) {
            return false;
        }
        if (to != null && timestamp.isAfter(to)) {
            return false;
        }
        return true;
    }

    private User findUserById(Long userId) {
        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id: " + userId);
        }
        return user.get();
    }

    private Account findAccountById(Long accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        if (account.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + accountId);
        }
        return account.get();
    }

    private KycCase findKycCaseById(Long caseId) {
        return kycCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "KYC case not found with id: " + caseId
                ));
    }

    private ResponseStatusException remapWithdrawException(ResponseStatusException exception) {
        if (exception.getStatusCode().value() == 400 && reasonContains(exception, "Insufficient funds")) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, exception.getReason(), exception);
        }
        if (exception.getStatusCode().value() == 400 && reasonContains(exception, "not active")) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, exception.getReason(), exception);
        }
        return exception;
    }

    private ResponseStatusException remapTransferException(ResponseStatusException exception) {
        if (exception.getStatusCode().value() == 400 && reasonContains(exception, "Insufficient funds")) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, exception.getReason(), exception);
        }
        if (exception.getStatusCode().value() == 400 && reasonContains(exception, "not active")) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, exception.getReason(), exception);
        }
        return exception;
    }

    private boolean reasonContains(ResponseStatusException exception, String text) {
        return exception.getReason() != null && exception.getReason().contains(text);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

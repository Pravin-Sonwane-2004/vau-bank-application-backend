package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.dtos.accountdtos.BalanceResponseDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.Transaction;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.account.AccountType;
import com.vaul.vaul.enums.transaction.TransactionType;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepository;
import com.vaul.vaul.services.interfaces.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountServiceImpl implements AccountService {

    private static final BigDecimal MIN_SAVINGS_OPENING_DEPOSIT = new BigDecimal("1000.00");
    private static final BigDecimal MIN_CURRENT_OPENING_DEPOSIT = new BigDecimal("5000.00");

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public AccountResponseDto openAccount(AccountOpenRequestDto requestDto) {
        User user = findUserById(requestDto.getUserId());
        BigDecimal initialDeposit = normalizeNonNegativeAmount(requestDto.getInitialDeposit(), "Initial deposit");
        validateMinimumOpeningDeposit(requestDto.getAccountType(), initialDeposit);

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(requestDto.getAccountType());
        account.setBalance(initialDeposit);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBranchCode(requestDto.getBranch().getBranchCode());
        account.setOpenedAt(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);
        saveTransaction(
                TransactionType.DEPOSIT,
                null,
                savedAccount.getId(),
                initialDeposit,
                initialDeposit,
                null,
                "Initial deposit during account opening"
        );

        return mapToAccountResponse(savedAccount, "Account opened successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long accountId) {
        return mapToAccountResponse(findAccountById(accountId), null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByNumber(String accountNumber) {
        return mapToAccountResponse(findAccountByNumber(accountNumber), null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }

        List<Account> accounts = accountRepository.findByUserId(userId);
        List<AccountResponseDto> responseList = new ArrayList<>();
        for (Account account : accounts) {
            responseList.add(mapToAccountResponse(account, null));
        }
        return responseList;
    }

    @Override
    @Transactional
    public AccountResponseDto deposit(DepositRequestDto requestDto) {
        Account account = findAccountByIdForUpdate(requestDto.getAccountId());
        requireActiveAccount(account);

        BigDecimal amount = normalizePositiveAmount(requestDto.getAmount(), "Amount");
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);

        Account savedAccount = accountRepository.save(account);
        saveTransaction(
                TransactionType.DEPOSIT,
                null,
                savedAccount.getId(),
                amount,
                newBalance,
                null,
                "Money deposited successfully"
        );

        return mapToAccountResponse(savedAccount, "Money deposited successfully");
    }

    @Override
    @Transactional
    public AccountResponseDto withdraw(WithdrawRequestDto requestDto) {
        Account account = findAccountByIdForUpdate(requestDto.getAccountId());
        requireActiveAccount(account);

        BigDecimal amount = normalizePositiveAmount(requestDto.getAmount(), "Amount");
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);

        Account savedAccount = accountRepository.save(account);
        saveTransaction(
                TransactionType.WITHDRAW,
                savedAccount.getId(),
                null,
                amount,
                newBalance,
                null,
                "Money withdrawn successfully"
        );

        return mapToAccountResponse(savedAccount, "Money withdrawn successfully");
    }

    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto requestDto) {
        if (requestDto.getFromAccountId().equals(requestDto.getToAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer requires two different accounts");
        }

        BigDecimal amount = normalizePositiveAmount(requestDto.getAmount(), "Amount");
        Account[] lockedAccounts = lockAccountsForTransfer(requestDto.getFromAccountId(), requestDto.getToAccountId());
        Account fromAccount = lockedAccounts[0];
        Account toAccount = lockedAccounts[1];

        requireActiveAccount(fromAccount);
        requireActiveAccount(toAccount);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal fromBalanceAfter = fromAccount.getBalance().subtract(amount);
        BigDecimal toBalanceAfter = toAccount.getBalance().add(amount);

        fromAccount.setBalance(fromBalanceAfter);
        toAccount.setBalance(toBalanceAfter);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = saveTransaction(
                TransactionType.TRANSFER,
                fromAccount.getId(),
                toAccount.getId(),
                amount,
                fromBalanceAfter,
                toBalanceAfter,
                "Money transferred successfully"
        );

        return mapToTransactionResponse(transaction, "Money transferred successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsByAccountId(Long accountId) {
        findAccountById(accountId);

        List<Transaction> transactions = transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByTimestampDesc(accountId, accountId);
        List<TransactionResponseDto> responseList = new ArrayList<>();
        for (Transaction transaction : transactions) {
            responseList.add(mapToTransactionResponse(transaction, null));
        }
        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponseDto getBalance(Long accountId) {
        Account account = findAccountById(accountId);
        return new BalanceResponseDto(account.getId(), account.getAccountNumber(), account.getBalance());
    }

    @Override
    @Transactional
    public AccountResponseDto blockAccount(Long accountId) {
        return changeAccountStatus(accountId, AccountStatus.BLOCKED, "Account blocked successfully");
    }

    @Override
    @Transactional
    public AccountResponseDto activateAccount(Long accountId) {
        return changeAccountStatus(accountId, AccountStatus.ACTIVE, "Account activated successfully");
    }

    @Override
    @Transactional
    public AccountResponseDto closeAccount(Long accountId) {
        Account account = findAccountByIdForUpdate(accountId);

        if (account.getStatus() == AccountStatus.CLOSED) {
            return mapToAccountResponse(account, "Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account balance must be zero before closing");
        }

        account.setStatus(AccountStatus.CLOSED);
        Account savedAccount = accountRepository.save(account);
        return mapToAccountResponse(savedAccount, "Account closed successfully");
    }

    private User findUserById(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }
        return userOpt.get();
    }

    private Account findAccountByNumber(String accountNumber) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with number: " + accountNumber);
        }
        return accountOpt.get();
    }

    private Account findAccountById(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + accountId);
        }
        return accountOpt.get();
    }

    private Account findAccountByIdForUpdate(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findByIdForUpdate(accountId);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + accountId);
        }
        return accountOpt.get();
    }

    private Account[] lockAccountsForTransfer(Long fromAccountId, Long toAccountId) {
        Long firstId = fromAccountId < toAccountId ? fromAccountId : toAccountId;
        Long secondId = fromAccountId < toAccountId ? toAccountId : fromAccountId;

        Account firstAccount = findAccountByIdForUpdate(firstId);
        Account secondAccount = findAccountByIdForUpdate(secondId);

        if (fromAccountId.equals(firstId)) {
            return new Account[]{firstAccount, secondAccount};
        }

        return new Account[]{secondAccount, firstAccount};
    }

    private AccountResponseDto changeAccountStatus(Long accountId, AccountStatus targetStatus, String successMessage) {
        Account account = findAccountByIdForUpdate(accountId);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Closed account cannot change status");
        }

        if (account.getStatus() == targetStatus) {
            return mapToAccountResponse(account, "Account is already " + targetStatus.name().toLowerCase());
        }

        account.setStatus(targetStatus);
        Account savedAccount = accountRepository.save(account);
        return mapToAccountResponse(savedAccount, successMessage);
    }

    private void requireActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account " + account.getId() + " is not active"
            );
        }
    }

    private void validateMinimumOpeningDeposit(AccountType accountType, BigDecimal initialDeposit) {
        BigDecimal minimumOpeningDeposit = accountType == AccountType.SAVINGS
                ? MIN_SAVINGS_OPENING_DEPOSIT
                : MIN_CURRENT_OPENING_DEPOSIT;

        if (initialDeposit.compareTo(minimumOpeningDeposit) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    accountType + " account requires minimum opening deposit of " + minimumOpeningDeposit
            );
        }
    }

    private BigDecimal normalizePositiveAmount(BigDecimal amount, String fieldName) {
        BigDecimal normalizedAmount = normalizeScale(amount, fieldName);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be greater than 0");
        }
        return normalizedAmount;
    }

    private BigDecimal normalizeNonNegativeAmount(BigDecimal amount, String fieldName) {
        BigDecimal normalizedAmount = normalizeScale(amount, fieldName);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " cannot be negative");
        }
        return normalizedAmount;
    }

    private BigDecimal normalizeScale(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private Transaction saveTransaction(
            TransactionType type,
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            BigDecimal balanceAfter,
            BigDecimal destinationBalanceAfter,
            String description
    ) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDestinationBalanceAfter(destinationBalanceAfter);
        transaction.setDescription(description);
        return transactionRepository.save(transaction);
    }

    private AccountResponseDto mapToAccountResponse(Account account, String message) {
        AccountResponseDto responseDto = new AccountResponseDto();
        responseDto.setAccountId(account.getId());
        responseDto.setAccountNumber(account.getAccountNumber());
        responseDto.setAccountType(account.getAccountType());
        responseDto.setBalance(account.getBalance());
        responseDto.setStatus(account.getStatus());
        responseDto.setBranchCode(account.getBranchCode());
        responseDto.setOpenedAt(account.getOpenedAt());
        responseDto.setUserId(account.getUser().getId());
        responseDto.setUserName(account.getUser().getName());
        responseDto.setMessage(message);
        return responseDto;
    }

    private TransactionResponseDto mapToTransactionResponse(Transaction transaction, String message) {
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

    private String generateUniqueAccountNumber() {
        int year = Year.now().getValue();

        for (int attempt = 0; attempt < 25; attempt++) {
            String candidate = "%d%08d".formatted(
                    year,
                    ThreadLocalRandom.current().nextInt(0, 100_000_000)
            );

            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to generate a unique account number"
        );
    }
}

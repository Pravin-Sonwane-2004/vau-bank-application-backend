package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.dtos.accountdtos.BalanceResponseDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.Transaction;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.dtos.accountdtos.BalanceResponseDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.Transaction;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.enums.transaction.TransactionType;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepo;
import com.vaul.vaul.services.interfaces.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import java.util.Optional;
import java.util.ArrayList;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepo userRepo;
    private final TransactionRepository transactionRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepo userRepo, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepo = userRepo;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public AccountResponseDto openAccount(AccountOpenRequestDto requestDto) {
        Optional<User> userOpt = userRepo.findById(requestDto.getUserId());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found with id: " + requestDto.getUserId()
            );
        }
        User user = userOpt.get();

        BigDecimal initialDeposit = normalizeAmount(requestDto.getInitialDeposit());
        int branchCode = requestDto.getBranch().getBranchCode();

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(requestDto.getAccountType());
        account.setBalance(initialDeposit);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBranchCode(branchCode);
        account.setOpenedAt(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountById(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found with id: " + accountId
            );
        }
        Account account = accountOpt.get();

        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }

        List<Account> accounts = accountRepository.findByUserId(userId);
        List<AccountResponseDto> responseList = new ArrayList<>();
        for (Account account : accounts) {
            responseList.add(mapToResponse(account));
        }
        return responseList;
    }

    @Override
    @Transactional
    public AccountResponseDto deposit(DepositRequestDto requestDto) {
        Optional<Account> accountOpt = accountRepository.findById(requestDto.getAccountId());
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + requestDto.getAccountId());
        }
        Account account = accountOpt.get();

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not active");
        }

        BigDecimal amount = normalizeAmount(requestDto.getAmount());
        BigDecimal newBalance = account.getBalance().add(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setFromAccountId(account.getId());
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription("Deposit");
        transactionRepository.save(transaction);

        return mapToResponse(account);
    }

    @Override
    @Transactional
    public AccountResponseDto withdraw(WithdrawRequestDto requestDto) {
        Optional<Account> accountOpt = accountRepository.findById(requestDto.getAccountId());
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + requestDto.getAccountId());
        }
        Account account = accountOpt.get();

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not active");
        }

        BigDecimal amount = normalizeAmount(requestDto.getAmount());
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setFromAccountId(account.getId());
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription("Withdrawal");
        transactionRepository.save(transaction);

        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponseDto getBalance(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found with id: " + accountId);
        }
        Account account = accountOpt.get();

        return new BalanceResponseDto(account.getId(), account.getAccountNumber(), account.getBalance());
    }

    private AccountResponseDto mapToResponse(Account account) {
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
        return responseDto;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
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

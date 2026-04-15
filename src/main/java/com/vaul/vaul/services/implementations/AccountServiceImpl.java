package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.entities.Account;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.UserRepo;
import com.vaul.vaul.services.interfaces.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepo userRepo;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepo userRepo) {
        this.accountRepository = accountRepository;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public AccountResponseDto openAccount(AccountOpenRequestDto requestDto) {
        User user = userRepo.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + requestDto.getUserId()
                ));

        BigDecimal initialDeposit = normalizeAmount(requestDto.getInitialDeposit());
        String branchCode = normalizeBranchCode(requestDto.getBranchCode());

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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found with id: " + accountId
                ));

        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }

        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial deposit is required");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial deposit cannot be negative");
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeBranchCode(String branchCode) {
        if (branchCode == null || branchCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch code is required");
        }

        return branchCode.trim().toUpperCase();
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

package com.vaul.vaul.dtos.transactiondtos;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.dtos.accountdtos.BalanceResponseDto;
import com.vaul.vaul.services.interfaces.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/open")
    public ResponseEntity<AccountResponseDto> openAccount(@Valid @RequestBody AccountOpenRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.openAccount(requestDto));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponseDto> deposit(@Valid @RequestBody DepositRequestDto requestDto) {
        return ResponseEntity.ok(accountService.deposit(requestDto));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponseDto> withdraw(@Valid @RequestBody WithdrawRequestDto requestDto) {
        return ResponseEntity.ok(accountService.withdraw(requestDto));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransferRequestDto requestDto) {
        return ResponseEntity.ok(accountService.transfer(requestDto));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getTransactionsByAccountId(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponseDto> getBalance(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }
}

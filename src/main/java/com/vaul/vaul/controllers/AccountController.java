package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
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
}

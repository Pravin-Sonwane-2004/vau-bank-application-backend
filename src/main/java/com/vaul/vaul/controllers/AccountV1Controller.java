package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.accountdtos.*;
import com.vaul.vaul.services.interfaces.BankingV1Service;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountV1Controller {

    private final BankingV1Service bankingV1Service;

    public AccountV1Controller(BankingV1Service bankingV1Service) {
        this.bankingV1Service = bankingV1Service;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> openAccount(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateAccountRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingV1Service.openAccount(idempotencyKey, requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(bankingV1Service.getAccount(id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponseDto> getBalance(@PathVariable Long id) {
        return ResponseEntity.ok(bankingV1Service.getBalance(id));
    }

    @GetMapping("/{id}/statements")
    public ResponseEntity<AccountStatementResponseDto> getStatement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(bankingV1Service.getStatement(id, from, to));
    }

    @PostMapping("/{id}/closure-requests")
    public ResponseEntity<AccountClosureResponseDto> requestClosure(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody(required = false) AccountClosureRequestCreateDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingV1Service.requestAccountClosure(idempotencyKey, id, requestDto));
    }
}

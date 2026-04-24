package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import com.vaul.vaul.services.interfaces.BankingV1Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionV1Controller {

    private final BankingV1Service bankingV1Service;

    public TransactionV1Controller(BankingV1Service bankingV1Service) {
        this.bankingV1Service = bankingV1Service;
    }

    @PostMapping("/deposits")
    public ResponseEntity<AccountResponseDto> deposit(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequestDto requestDto
    ) {
        return ResponseEntity.ok(bankingV1Service.deposit(idempotencyKey, requestDto));
    }

    @PostMapping("/withdrawals")
    public ResponseEntity<AccountResponseDto> withdraw(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequestDto requestDto
    ) {
        return ResponseEntity.ok(bankingV1Service.withdraw(idempotencyKey, requestDto));
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransactionResponseDto> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequestDto requestDto
    ) {
        return ResponseEntity.ok(bankingV1Service.transfer(idempotencyKey, requestDto));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(bankingV1Service.getTransaction(id));
    }
}

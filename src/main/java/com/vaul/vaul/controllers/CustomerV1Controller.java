package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.customerdtos.CreateCustomerRequestDto;
import com.vaul.vaul.dtos.customerdtos.CustomerResponseDto;
import com.vaul.vaul.services.interfaces.BankingV1Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerV1Controller {

    private final BankingV1Service bankingV1Service;

    public CustomerV1Controller(BankingV1Service bankingV1Service) {
        this.bankingV1Service = bankingV1Service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateCustomerRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingV1Service.createCustomer(idempotencyKey, requestDto));
    }
}

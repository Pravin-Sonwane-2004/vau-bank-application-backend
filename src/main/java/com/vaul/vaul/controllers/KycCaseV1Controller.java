package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.kycdtos.CreateKycCaseRequestDto;
import com.vaul.vaul.dtos.kycdtos.KycCaseResponseDto;
import com.vaul.vaul.dtos.kycdtos.KycDecisionRequestDto;
import com.vaul.vaul.services.interfaces.BankingV1Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc/cases")
public class KycCaseV1Controller {

    private final BankingV1Service bankingV1Service;

    public KycCaseV1Controller(BankingV1Service bankingV1Service) {
        this.bankingV1Service = bankingV1Service;
    }

    @PostMapping
    public ResponseEntity<KycCaseResponseDto> submitKycCase(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateKycCaseRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankingV1Service.submitKycCase(idempotencyKey, requestDto));
    }

    @PatchMapping("/{id}/decision")
    public ResponseEntity<KycCaseResponseDto> decideKycCase(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody KycDecisionRequestDto requestDto
    ) {
        return ResponseEntity.ok(bankingV1Service.decideKycCase(idempotencyKey, id, requestDto));
    }
}

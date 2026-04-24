package com.vaul.vaul.dtos.kycdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaul.vaul.enums.kyc.KycCaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycCaseResponseDto {
    private Long id;
    private Long customerId;
    private KycCaseStatus status;
    private String documentType;
    private String documentNumber;
    private String addressLine;
    private String reviewNotes;
    private LocalDateTime submittedAt;
    private LocalDateTime decidedAt;
    private String message;
}

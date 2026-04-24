package com.vaul.vaul.dtos.kycdtos;

import com.vaul.vaul.enums.kyc.KycCaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KycDecisionRequestDto {

    @NotNull(message = "Decision is required")
    private KycCaseStatus decision;

    private String reviewNotes;
}

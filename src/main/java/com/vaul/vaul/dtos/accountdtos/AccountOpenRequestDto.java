package com.vaul.vaul.dtos.accountdtos;

import com.vaul.vaul.enums.account.AccountType;
import com.vaul.vaul.enums.branches.ExistsBranches;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountOpenRequestDto {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial deposit is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    @NotNull(message = "Branch is required")
    private ExistsBranches branch;
}

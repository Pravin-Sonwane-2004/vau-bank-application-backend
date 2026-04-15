package com.vaul.vaul.dtos.accountdtos;

import com.vaul.vaul.enums.account.AccountType;
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

    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Branch code must be at most 20 characters")
    private String branchCode;
}

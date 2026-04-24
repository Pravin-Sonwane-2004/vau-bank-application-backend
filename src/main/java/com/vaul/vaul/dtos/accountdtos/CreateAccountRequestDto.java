package com.vaul.vaul.dtos.accountdtos;

import com.vaul.vaul.enums.account.AccountType;
import com.vaul.vaul.enums.branches.ExistsBranches;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDto {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial deposit is required")
    private BigDecimal initialDeposit;

    @NotNull(message = "Branch is required")
    private ExistsBranches branch;
}

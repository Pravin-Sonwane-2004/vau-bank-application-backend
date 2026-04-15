package com.vaul.vaul.dtos.accountdtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BalanceResponseDto {
    private Long accountId;
    private String accountNumber;
    private BigDecimal balance;
}

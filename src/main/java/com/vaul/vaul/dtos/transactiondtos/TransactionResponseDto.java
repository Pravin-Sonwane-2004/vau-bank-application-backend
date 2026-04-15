package com.vaul.vaul.dtos.transactiondtos;

import com.vaul.vaul.enums.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionResponseDto {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal balanceAfter;
    private LocalDateTime timestamp;
    private String description;
}

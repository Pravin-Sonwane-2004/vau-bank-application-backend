package com.vaul.vaul.dtos.accountdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountStatementResponseDto {
    private Long accountId;
    private String accountNumber;
    private BigDecimal currentBalance;
    private LocalDateTime from;
    private LocalDateTime to;
    private LocalDateTime generatedAt;
    private List<TransactionResponseDto> transactions;
}

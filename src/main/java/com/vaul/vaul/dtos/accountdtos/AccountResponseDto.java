package com.vaul.vaul.dtos.accountdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaul.vaul.enums.account.AccountStatus;
import com.vaul.vaul.enums.account.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AccountResponseDto {

    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private int branchCode;
    private LocalDateTime openedAt;
    private Long userId;
    private String userName;
    private String message;
}

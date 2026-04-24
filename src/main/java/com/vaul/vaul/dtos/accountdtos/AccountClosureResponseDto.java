package com.vaul.vaul.dtos.accountdtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaul.vaul.enums.account.ClosureRequestStatus;
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
public class AccountClosureResponseDto {
    private Long id;
    private Long accountId;
    private ClosureRequestStatus status;
    private String reason;
    private LocalDateTime requestedAt;
    private String message;
}

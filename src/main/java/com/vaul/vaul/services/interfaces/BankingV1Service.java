package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.accountdtos.*;
import com.vaul.vaul.dtos.customerdtos.CreateCustomerRequestDto;
import com.vaul.vaul.dtos.customerdtos.CustomerResponseDto;
import com.vaul.vaul.dtos.kycdtos.CreateKycCaseRequestDto;
import com.vaul.vaul.dtos.kycdtos.KycCaseResponseDto;
import com.vaul.vaul.dtos.kycdtos.KycDecisionRequestDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;

import java.time.LocalDateTime;

public interface BankingV1Service {
    CustomerResponseDto createCustomer(String idempotencyKey, CreateCustomerRequestDto requestDto);

    KycCaseResponseDto submitKycCase(String idempotencyKey, CreateKycCaseRequestDto requestDto);

    KycCaseResponseDto decideKycCase(String idempotencyKey, Long caseId, KycDecisionRequestDto requestDto);

    AccountResponseDto openAccount(String idempotencyKey, CreateAccountRequestDto requestDto);

    AccountResponseDto getAccount(Long accountId);

    BalanceResponseDto getBalance(Long accountId);

    AccountResponseDto deposit(String idempotencyKey, DepositRequestDto requestDto);

    AccountResponseDto withdraw(String idempotencyKey, WithdrawRequestDto requestDto);

    TransactionResponseDto transfer(String idempotencyKey, TransferRequestDto requestDto);

    TransactionResponseDto getTransaction(Long transactionId);

    AccountStatementResponseDto getStatement(Long accountId, LocalDateTime from, LocalDateTime to);

    AccountClosureResponseDto requestAccountClosure(
            String idempotencyKey,
            Long accountId,
            AccountClosureRequestCreateDto requestDto
    );
}

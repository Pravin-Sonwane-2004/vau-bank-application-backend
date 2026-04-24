package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;
import com.vaul.vaul.dtos.accountdtos.BalanceResponseDto;
import com.vaul.vaul.dtos.transactiondtos.DepositRequestDto;
import com.vaul.vaul.dtos.transactiondtos.TransactionResponseDto;
import com.vaul.vaul.dtos.transactiondtos.TransferRequestDto;
import com.vaul.vaul.dtos.transactiondtos.WithdrawRequestDto;
import java.util.List;

public interface AccountService {
    AccountResponseDto openAccount(AccountOpenRequestDto requestDto);

    AccountResponseDto getAccountById(Long accountId);

    AccountResponseDto getAccountByNumber(String accountNumber);

    List<AccountResponseDto> getAccountsByUserId(Long userId);

    AccountResponseDto deposit(DepositRequestDto requestDto);

    AccountResponseDto withdraw(WithdrawRequestDto requestDto);

    TransactionResponseDto transfer(TransferRequestDto requestDto);

    List<TransactionResponseDto> getTransactionsByAccountId(Long accountId);

    BalanceResponseDto getBalance(Long accountId);

    AccountResponseDto blockAccount(Long accountId);

    AccountResponseDto activateAccount(Long accountId);

    AccountResponseDto closeAccount(Long accountId);
}

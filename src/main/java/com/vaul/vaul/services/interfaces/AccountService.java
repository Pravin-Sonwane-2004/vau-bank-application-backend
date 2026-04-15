package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.accountdtos.AccountOpenRequestDto;
import com.vaul.vaul.dtos.accountdtos.AccountResponseDto;

import java.util.List;

public interface AccountService {
    AccountResponseDto openAccount(AccountOpenRequestDto requestDto);

    AccountResponseDto getAccountById(Long accountId);

    List<AccountResponseDto> getAccountsByUserId(Long userId);
}

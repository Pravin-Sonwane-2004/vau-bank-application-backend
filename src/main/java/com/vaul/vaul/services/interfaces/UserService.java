package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.UserRequestDto;
import com.vaul.vaul.dtos.userdtos.UserResponseDto;
import com.vaul.vaul.enums.branches.ExistsBranches;

import java.util.List;

public interface UserService {
    UserResponseDto registerUser(UserRequestDto dto);

    LoginResponseDto loginUser(LoginRequestDto dto);

    List<UserResponseDto> fetchUser();

    UserResponseDto getOneUser(Long id);

    List<UserResponseDto> addBulkUsers(List<UserRequestDto> dto);

    UserResponseDto updateUserById(Long id, UserRequestDto dto);

    UserResponseDto updateUserByEmail(String email, UserRequestDto dto);

    List<ExistsBranches> returnAllBranches();
}

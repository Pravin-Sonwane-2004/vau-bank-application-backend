package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.enums.branches.ExistsBranches;

import java.util.List;

public interface UserService {
    responseRegistration registerUser(registerRequestDto dto);

    LoginResponseDto loginUser(LoginRequestDto dto);

    List<responseRegistration> fetchUser();

    responseRegistration getOneUser(Long id);

    List<responseRegistration> addBulkUsers(List<registerRequestDto> dto);

    responseRegistration updateUserById(Long id, registerRequestDto dto);

    responseRegistration updateUserByEmail(String email, registerRequestDto dto);

    List<ExistsBranches> returnAllBranches();
}

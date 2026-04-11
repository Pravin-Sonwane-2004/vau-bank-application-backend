package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import jakarta.validation.constraints.Email;
//import com.vaul.vaul.model.User;

import java.util.List;

public interface UserService {
    responseRegistration registerUser(registerRequestDto dto);

    List<responseRegistration> fetchUser();

    responseRegistration getOneUser(Long id);

    List<responseRegistration> addBulkUsers(List<registerRequestDto> dto);

    responseRegistration updateUserById(Long id, registerRequestDto dto);

    responseRegistration updateUserByEmail(String email, registerRequestDto dto);
}
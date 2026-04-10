package com.vaul.vaul.services.interfaces;

import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
//import com.vaul.vaul.model.User;

import java.util.List;

public interface UserService {
    responseRegistration registerUser(registerRequestDto dto);

    List<responseRegistration> fetchUser();

    responseRegistration getOneUser(Long id);

    responseRegistration updateUserById(Long id, registerRequestDto dto);

    List<responseRegistration> addBulkUsers(List<registerRequestDto> dto);
}

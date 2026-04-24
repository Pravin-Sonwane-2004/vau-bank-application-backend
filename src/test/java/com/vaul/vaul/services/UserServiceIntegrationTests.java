package com.vaul.vaul.services;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.UserRequestDto;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.repositories.AccountRepository;
import com.vaul.vaul.repositories.TransactionRepository;
import com.vaul.vaul.repositories.UserRepository;
import com.vaul.vaul.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServiceIntegrationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void loginShouldSucceedForRegisteredUser() {
        UserRequestDto registerDto = new UserRequestDto(
                "Login User",
                "login-user@example.com",
                "pass123",
                9999999999L,
                null
        );
        userService.registerUser(registerDto);

        LoginResponseDto loginResponse = userService.loginUser(
                new LoginRequestDto("login-user@example.com", "pass123")
        );

        assertThat(loginResponse.getEmail()).isEqualTo("login-user@example.com");
        assertThat(loginResponse.getMessage()).isEqualTo("Login successful");
    }

    @Test
    void loginShouldFailForWrongPassword() {
        User user = new User();
        user.setName("Wrong Password User");
        user.setEmail("wrong-password@example.com");
        user.setPassword("correct-pass");
        user.setPhone(9999999999L);
        userRepo.save(user);

        try {
            userService.loginUser(new LoginRequestDto("wrong-password@example.com", "wrong-pass"));
        } catch (ResponseStatusException exception) {
            assertThat(exception.getStatusCode().value()).isEqualTo(401);
            assertThat(exception.getReason()).isEqualTo("Invalid email or password");
            return;
        }

        throw new AssertionError("Expected login to fail for wrong password");
    }
}

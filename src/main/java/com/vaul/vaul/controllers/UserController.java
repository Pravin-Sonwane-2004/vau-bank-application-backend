package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.UserRequestDto;
import com.vaul.vaul.dtos.userdtos.UserResponseDto;
import com.vaul.vaul.services.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(service.loginUser(loginRequestDto));
    }

    @PostMapping("/add")
    public ResponseEntity<UserResponseDto> addUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(service.registerUser(userRequestDto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<UserResponseDto>> addBulkUsers(
            @Valid @RequestBody List<UserRequestDto> userRequests) {
        return ResponseEntity.ok(service.addBulkUsers(userRequests));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> updateUserById(
            @PathVariable Long id,
            @RequestBody UserRequestDto dto) {

        return ResponseEntity.ok(service.updateUserById(id, dto));
    }

    @PutMapping("/updatebyemail/{email}")
    public ResponseEntity<UserResponseDto> updateUserByEmail(
            @PathVariable String email,
            @RequestBody UserRequestDto dto) {

        return ResponseEntity.ok(service.updateUserByEmail(email, dto));
    }

    @GetMapping("/getall")
    public ResponseEntity<List<UserResponseDto>> fetch() {
        return ResponseEntity.ok(service.fetchUser());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOneUser(id));
    }

}

package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.services.implementations.UserService;
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
    public ResponseEntity<responseRegistration> addUser(@Valid @RequestBody registerRequestDto registerRequestDto) {
        return ResponseEntity.ok(service.registerUser(registerRequestDto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<responseRegistration>> addBulkUsers(
            @Valid @RequestBody List<registerRequestDto> registerRequests) {
        return ResponseEntity.ok(service.addBulkUsers(registerRequests));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<responseRegistration> updateUserById(
            @PathVariable Long id,
            @RequestBody registerRequestDto dto) {

        return ResponseEntity.ok(service.updateUserById(id, dto));
    }

    @PutMapping("/updatebyemail/{email}")
    public ResponseEntity<responseRegistration> updateUserByEmail(
            @PathVariable String email,
            @RequestBody registerRequestDto dto) {

        return ResponseEntity.ok(service.updateUserByEmail(email, dto));
    }

    @GetMapping("/getall")
    public ResponseEntity<List<responseRegistration>> fetch() {
        return ResponseEntity.ok(service.fetchUser());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<responseRegistration> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOneUser(id));
    }

}

package com.vaul.vaul.controllers;

import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.services.implementations.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/add")
        public ResponseEntity<responseRegistration> addUser(@Valid @RequestBody registerRequestDto registerRequestDto) {
        return ResponseEntity.ok(service.registerUser(registerRequestDto));
    }

    @GetMapping("/getall")
    public ResponseEntity<List<responseRegistration>>fetch() {
        return ResponseEntity.ok(service.fetchUser());
    }

    @GetMapping("get/{id}")
    public  ResponseEntity<responseRegistration> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOneUser(id));
    }


}

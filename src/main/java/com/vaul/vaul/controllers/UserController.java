package com.vaul.vaul.controllers;

import com.vaul.vaul.model.User;
import com.vaul.vaul.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/add")
        public ResponseEntity<User> add(@RequestBody User user) {
        return ResponseEntity.ok(service.addUser(user));
    }

    @GetMapping("/getallusers")
    public ResponseEntity<?>fetch() {
        return ResponseEntity.ok(service.fetchUser());
    }
}

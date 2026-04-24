package com.vaul.vaul.controllers;

import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.services.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branch")
public class BranchController {

    private final UserService service;

    public BranchController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ExistsBranches>> getAllBranches() {
        return ResponseEntity.ok(service.returnAllBranches());
    }
}

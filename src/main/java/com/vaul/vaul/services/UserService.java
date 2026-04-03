package com.vaul.vaul.services;

import com.vaul.vaul.model.User;
import com.vaul.vaul.repositories.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserService {

    private final UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    public User addUser(User user) {
        return repo.save(user);
    }

    public List<User> fetchUser() {
        return repo.findAll();
    }

}

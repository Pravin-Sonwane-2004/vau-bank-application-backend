package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.repositories.UserRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements com.vaul.vaul.services.interfaces.UserService {

    private final UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    public responseRegistration registerUser(registerRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        User savedUser = repo.save(user);

        responseRegistration response = new responseRegistration();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());

        return response;
    }

    @Override
    public List<responseRegistration> fetchUser() {
        List<User> users = repo.findAll();
        List<responseRegistration> returnDto = new ArrayList<>();
        for (User user : users) {
            responseRegistration dto = new responseRegistration();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());

            returnDto.add(dto);
        }
        return returnDto;
    }
    @Override
    public responseRegistration getOneUser(Long id) {
        //  Fetch user safely
        User user = repo.findById(id)
                .orElseThrow(() ->  new RuntimeException("User not found with id: " + id));

        //  Convert Entity → DTO
        responseRegistration response = new responseRegistration();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return response;
    }
    @Override
    public responseRegistration updateUserById(Long id, registerRequestDto dto) {

        // 🔹 Fetch existing user
        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔹 Update fields
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        // 🔹 Save updated user
        User updatedUser = repo.save(user);

        // 🔹 Convert to response DTO
        responseRegistration response = new responseRegistration();
        response.setId(updatedUser.getId());
        response.setName(updatedUser.getName());
        response.setEmail(updatedUser.getEmail());

        return response;
    }


}
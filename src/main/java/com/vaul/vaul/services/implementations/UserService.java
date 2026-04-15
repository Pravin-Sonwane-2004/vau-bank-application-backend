package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.exceptions.userrelated.UserAlredyPresent;
import com.vaul.vaul.exceptions.userrelated.UserNotFoundException;
import com.vaul.vaul.repositories.UserRepo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        user.setImage(dto.getImage());
        user.setPhone(dto.getPhone());
        User savedUser = repo.save(user);

        responseRegistration response = new responseRegistration();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setImage(savedUser.getImage());
        response.setPhone(savedUser.getPhone());

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
            dto.setPhone(user.getPhone());
            dto.setImage(user.getImage());
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
        response.setPhone(user.getPhone());
        return response;
    }
    @Override
    public responseRegistration updateUserById(Long id, registerRequestDto dto) {

        User user = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }

        User updatedUser = repo.save(user);

        responseRegistration response = new responseRegistration();
        response.setId(updatedUser.getId());
        response.setName(updatedUser.getName());
        response.setEmail(updatedUser.getEmail());
        response.setPhone(user.getPhone());

        return response;
    }
    @Override
    public List<responseRegistration> addBulkUsers(List<registerRequestDto> dto) {
        List<User> users = new ArrayList<>();
        for (registerRequestDto dtos : dto) {
            User user = new User();
            user.setName(dtos.getName());
            user.setEmail(dtos.getEmail());
            user.setPassword(dtos.getPassword());
            users.add(user);
        }
        // Save all users
        List<User> savedUsers = repo.saveAll(users);
        // Convert Entity -> Response DTO
        List<responseRegistration> responseList = new ArrayList<>();
        for (User user : savedUsers) {
            responseRegistration response = new responseRegistration();
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            responseList.add(response);
            response.setPhone(user.getPhone());
        }
        return responseList;
    }

    @Override
    public responseRegistration updateUserByEmail(String email, registerRequestDto dto) {

        Optional<User> optionalUser = repo.findByEmail(email);

        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException(email);
        }
        if(dto.getEmail().equals(repo.findByEmail(email))) {
            throw new UserAlredyPresent(email);
        }

        User user = optionalUser.get();

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null) {
            user.setPassword(dto.getPassword());
        }

        User updatedUser = repo.save(user);

        responseRegistration response = new responseRegistration();
        response.setId(updatedUser.getId());
        response.setName(updatedUser.getName());
        response.setEmail(updatedUser.getEmail());

        return response;
    }
}
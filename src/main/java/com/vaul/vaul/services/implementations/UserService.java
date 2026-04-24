package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.registerRequestDto;
import com.vaul.vaul.dtos.userdtos.responseRegistration;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.exceptions.userrelated.UserAlredyPresent;
import com.vaul.vaul.exceptions.userrelated.UserNotFoundException;
import com.vaul.vaul.repositories.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements com.vaul.vaul.services.interfaces.UserService {

    private final UserRepo repo;

    public UserService(UserRepo repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public responseRegistration registerUser(registerRequestDto dto) {
        ensureEmailIsAvailable(dto.getEmail(), null);
        User savedUser = repo.save(buildUser(dto));
        return toResponse(savedUser, "User registered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto loginUser(LoginRequestDto dto) {
        Optional<User> userOpt = repo.findByEmail(dto.getEmail());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return new LoginResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                "Login successful"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<responseRegistration> fetchUser() {
        List<User> users = repo.findAll();
        List<responseRegistration> responseList = new ArrayList<>();
        for (User user : users) {
            responseList.add(toResponse(user, null));
        }
        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public responseRegistration getOneUser(Long id) {
        return toResponse(findUserById(id), null);
    }

    @Override
    @Transactional
    public List<responseRegistration> addBulkUsers(List<registerRequestDto> dtoList) {
        List<User> users = new ArrayList<>();
        for (registerRequestDto dto : dtoList) {
            ensureEmailIsAvailable(dto.getEmail(), null);
            users.add(buildUser(dto));
        }

        List<User> savedUsers = repo.saveAll(users);
        List<responseRegistration> responseList = new ArrayList<>();
        for (User user : savedUsers) {
            responseList.add(toResponse(user, "User registered successfully"));
        }
        return responseList;
    }

    @Override
    @Transactional
    public responseRegistration updateUserById(Long id, registerRequestDto dto) {
        User user = findUserById(id);
        applyUpdates(user, dto);
        User updatedUser = repo.save(user);
        return toResponse(updatedUser, "User updated successfully");
    }

    @Override
    @Transactional
    public responseRegistration updateUserByEmail(String email, registerRequestDto dto) {
        Optional<User> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(email);
        }

        User user = userOpt.get();
        applyUpdates(user, dto);
        User updatedUser = repo.save(user);
        return toResponse(updatedUser, "User updated successfully");
    }

    @Override
    public List<ExistsBranches> returnAllBranches() {
        return Arrays.asList(ExistsBranches.values());
    }

    private User buildUser(registerRequestDto dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setImage(dto.getImage());
        return user;
    }

    private User findUserById(Long id) {
        Optional<User> userOpt = repo.findById(id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(id);
        }
        return userOpt.get();
    }

    private void applyUpdates(User user, registerRequestDto dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            ensureEmailIsAvailable(dto.getEmail(), user.getId());
            user.setEmail(dto.getEmail());
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }

        if (dto.getImage() != null) {
            user.setImage(dto.getImage());
        }
    }

    private void ensureEmailIsAvailable(String email, Long currentUserId) {
        Optional<User> existingUser = repo.findByEmail(email);
        if (existingUser.isPresent()) {
            if (currentUserId == null || !existingUser.get().getId().equals(currentUserId)) {
                throw new UserAlredyPresent(email);
            }
        }
    }

    private responseRegistration toResponse(User user, String message) {
        responseRegistration response = new responseRegistration();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setImage(user.getImage());
        response.setMessage(message);
        return response;
    }
}

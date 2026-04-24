package com.vaul.vaul.services.implementations;

import com.vaul.vaul.dtos.userdtos.LoginRequestDto;
import com.vaul.vaul.dtos.userdtos.LoginResponseDto;
import com.vaul.vaul.dtos.userdtos.UserRequestDto;
import com.vaul.vaul.dtos.userdtos.UserResponseDto;
import com.vaul.vaul.entities.User;
import com.vaul.vaul.enums.branches.ExistsBranches;
import com.vaul.vaul.exceptions.userrelated.UserAlreadyPresentException;
import com.vaul.vaul.exceptions.userrelated.UserNotFoundException;
import com.vaul.vaul.repositories.UserRepository;
import com.vaul.vaul.services.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRequestDto dto) {
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
    public List<UserResponseDto> fetchUser() {
        List<User> users = repo.findAll();
        List<UserResponseDto> responseList = new ArrayList<>();
        for (User user : users) {
            responseList.add(toResponse(user, null));
        }
        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getOneUser(Long id) {
        return toResponse(findUserById(id), null);
    }

    @Override
    @Transactional
    public List<UserResponseDto> addBulkUsers(List<UserRequestDto> dtoList) {
        List<User> users = new ArrayList<>();
        for (UserRequestDto dto : dtoList) {
            ensureEmailIsAvailable(dto.getEmail(), null);
            users.add(buildUser(dto));
        }

        List<User> savedUsers = repo.saveAll(users);
        List<UserResponseDto> responseList = new ArrayList<>();
        for (User user : savedUsers) {
            responseList.add(toResponse(user, "User registered successfully"));
        }
        return responseList;
    }

    @Override
    @Transactional
    public UserResponseDto updateUserById(Long id, UserRequestDto dto) {
        User user = findUserById(id);
        applyUpdates(user, dto);
        User updatedUser = repo.save(user);
        return toResponse(updatedUser, "User updated successfully");
    }

    @Override
    @Transactional
    public UserResponseDto updateUserByEmail(String email, UserRequestDto dto) {
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
    @Transactional(readOnly = true)
    public List<ExistsBranches> returnAllBranches() {
        return Arrays.asList(ExistsBranches.values());
    }

    private User buildUser(UserRequestDto dto) {
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

    private void applyUpdates(User user, UserRequestDto dto) {
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
                throw new UserAlreadyPresentException(email);
            }
        }
    }

    private UserResponseDto toResponse(User user, String message) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setImage(user.getImage());
        response.setMessage(message);
        return response;
    }
}

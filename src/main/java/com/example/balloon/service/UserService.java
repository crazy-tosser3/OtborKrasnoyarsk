package com.example.balloon.service;

import com.example.balloon.exception.*;
import com.example.balloon.model.dto.user.request.*;
import com.example.balloon.model.dto.user.response.*;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.model.enums.RoleEnum;
import com.example.balloon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public UserLoginResponse login(UserLoginRequest req) {
        UserEntity user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new UnauthorizedException("invalid username or password"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("invalid username or password");
        }
        return new UserLoginResponse(user.getId(), user.getUsername());
    }

    @Transactional
    public UserRegisterResponse register(UserRegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("user already exists");
        }
        byte[] salt = passwordService.generateSalt();

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setUserEmail(req.getUserEmail());
        user.setPasswordHash(passwordService.hashPassword(req.getUserPassword(), salt));
        user.setSalt(passwordService.encodeSalt(salt));
        user.setRole(RoleEnum.USER);
        user.setEnabled(true);

        userRepository.save(user);
        return new UserRegisterResponse(user.getId(), user.getUsername(), user.getUserEmail());
    }

    @Transactional
    public UserUpdateResponse update(String currentUsername, UserUpdateRequest req) {
        UserEntity user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("invalid password");
        }

        String newUsername = req.getNewUsername() != null ? req.getNewUsername() : user.getUsername();
        if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
            throw new BadRequestException("new username already exists");
        }
        user.setUsername(newUsername);

        if (req.getNewUserPassword() != null && !req.getNewUserPassword().isBlank()) {
            byte[] newSalt = passwordService.generateSalt();
            user.setPasswordHash(passwordService.hashPassword(req.getNewUserPassword(), newSalt));
            user.setSalt(passwordService.encodeSalt(newSalt));
        }

        userRepository.save(user);
        return new UserUpdateResponse(user.getId(), currentUsername, user.getUsername());
    }

    @Transactional
    public void delete(String currentUsername, UserDeleteRequest req) {
        UserEntity user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("invalid password");
        }
        userRepository.delete(user);
    }

    public UserProfileResponse profile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user not found"));
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getUserEmail());
    }

    private boolean checkPassword(String rawPassword, UserEntity user) {
        byte[] salt = passwordService.decodeSalt(user.getSalt());
        String hash = passwordService.hashPassword(rawPassword, salt);
        return constantTimeEquals(hash, user.getPasswordHash());
    }

    private boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(
                a.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                b.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
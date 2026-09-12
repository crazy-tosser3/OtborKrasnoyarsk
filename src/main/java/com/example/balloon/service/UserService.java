package com.example.balloon.service;

import com.example.balloon.exception.*;
import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.*;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.model.enums.RoleEnum;
import com.example.balloon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public UserLoginResponse login(UserLoginRequest req) {
        UserEntity user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new UnauthorizedException("user not found"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("wrong password");
        }

        String token = jwtService.generateToken(user);
        return new UserLoginResponse(token, user.getUserRole());
    }

    @Transactional
    public MessageResponse register(UserRegisterRequest req) {
        if (userRepository.existsByUserName(req.getUserName())) {
            throw new ConflictException("user already exists");
        }
        byte[] salt = passwordService.generateSalt();

        UserEntity user = new UserEntity();
        user.setUserName(req.getUserName());
        user.setUserEmail(req.getUserEmail());
        user.setPasswordHash(passwordService.hashPassword(req.getUserPassword(), salt));
        user.setSalt(passwordService.encodeSalt(salt));
        user.setUserRole(RoleEnum.USER);

        userRepository.save(user);
        return new MessageResponse("user registered");
    }

    @Transactional
    public MessageResponse update(String currentUserName, UserUpdateRequest req) {
        UserEntity user = userRepository.findByUserName(currentUserName)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("wrong password");
        }

        String newUserName = req.getNewUserName() != null ? req.getNewUserName() : user.getUserName();
        if (!newUserName.equals(user.getUserName()) && userRepository.existsByUserName(newUserName)) {
            throw new BadRequestException("new username already exists");
        }
        user.setUserName(newUserName);

        if (req.getNewUserPassword() != null && !req.getNewUserPassword().isBlank()) {
            byte[] newSalt = passwordService.generateSalt();
            user.setPasswordHash(passwordService.hashPassword(req.getNewUserPassword(), newSalt));
            user.setSalt(passwordService.encodeSalt(newSalt));
        }

        userRepository.save(user);
        return new MessageResponse("user updated");
    }

    @Transactional
    public void delete(String currentUserName, UserDeleteRequest req) {
        UserEntity user = userRepository.findByUserName(currentUserName)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!checkPassword(req.getUserPassword(), user)) {
            throw new UnauthorizedException("wrong password");
        }
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(String userName) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
        return new UserProfileResponse(user.getUserName(), user.getUserEmail());
    }

    @Transactional(readOnly = true)
    public java.util.List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUserByUserName(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Transactional
    public void changeRole(String userName, RoleEnum newRole) {
        UserEntity user = getUserByUserName(userName);
        user.setUserRole(newRole);
        userRepository.save(user);
    }

    private boolean checkPassword(String rawPassword, UserEntity user) {
        byte[] salt = passwordService.decodeSalt(user.getSalt());
        String hash = passwordService.hashPassword(rawPassword, salt);
        return java.security.MessageDigest.isEqual(
                hash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                user.getPasswordHash().getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
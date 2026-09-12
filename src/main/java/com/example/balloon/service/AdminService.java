package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.user.ChangeRoleRequest;
import com.example.balloon.model.dto.user.UserResponse;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    public AdminService(UserRepository userRepository, EntityMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return mapper.toUserResponseList(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String userName) {
        return userRepository.findByUserName(userName)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Transactional
    public UserResponse changeRole(ChangeRoleRequest req) {
        UserEntity user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setUserRole(req.getRole());
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(String userName) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
        userRepository.delete(user);
    }
}
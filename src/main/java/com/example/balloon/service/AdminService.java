package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.user.request.ChangeRoleRequest;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {
    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserEntity getUser(String userName) {
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Transactional
    public UserEntity changeRole(ChangeRoleRequest req) {
        UserEntity user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setUserRole(req.getRole());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userName) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
        userRepository.delete(user);
    }
}
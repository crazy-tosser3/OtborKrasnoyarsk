package com.example.balloon.controller;

import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.UserLoginRequest;
import com.example.balloon.model.dto.user.UserRegisterRequest;
import com.example.balloon.model.dto.user.UserLoginResponse;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        MessageResponse response = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest req) {
        UserLoginResponse response = userService.login(req);
        return ResponseEntity.ok(response);
    }
}
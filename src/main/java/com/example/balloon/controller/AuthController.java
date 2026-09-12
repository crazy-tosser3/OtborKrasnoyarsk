package com.example.balloon.controller;

import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.UserLoginRequest;
import com.example.balloon.model.dto.user.UserLoginResponse;
import com.example.balloon.model.dto.user.UserRegisterRequest;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody UserRegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }
}
package com.example.balloon.controller;

import com.example.balloon.model.dto.user.request.UserLoginRequest;
import com.example.balloon.model.dto.user.request.UserRegisterRequest;
import com.example.balloon.model.dto.user.response.UserLoginResponse;
import com.example.balloon.model.dto.user.response.UserRegisterResponse;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public UserLoginResponse login(@Valid @RequestBody UserLoginRequest req) {
        return userService.login(req);
    }
}
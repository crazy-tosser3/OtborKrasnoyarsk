package com.example.balloon.controller;

import com.example.balloon.model.dto.user.request.UserLoginRequest;
import com.example.balloon.model.dto.user.request.UserRegisterRequest;
import com.example.balloon.model.dto.user.response.UserLoginResponse;
import com.example.balloon.model.dto.user.response.UserRegisterResponse;
import com.example.balloon.service.JwtService;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest req) {
        return userService.register(req);
    }

    @PostMapping("/login")
    public UserLoginTokenResponse login(@Valid @RequestBody UserLoginRequest req) {
        UserLoginResponse user = userService.login(req);
        String token = jwtService.generateTokenByUsername(user.getUsername());
        return new UserLoginTokenResponse(user, token);
    }

    public record UserLoginTokenResponse(UserLoginResponse user, String token) {}
}
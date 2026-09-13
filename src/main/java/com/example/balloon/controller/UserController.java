package com.example.balloon.controller;

import com.example.balloon.model.dto.user.*;
import com.example.balloon.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", userService.register(request)));
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> update(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(Map.of("message", userService.update(request)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> delete(@RequestBody UserDeleteRequest request) {
        return ResponseEntity.ok(Map.of("message", userService.delete(request)));
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }
}

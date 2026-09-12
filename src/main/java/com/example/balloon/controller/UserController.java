package com.example.balloon.controller;

import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.UserDeleteRequest;
import com.example.balloon.model.dto.user.UserUpdateRequest;
import com.example.balloon.model.dto.user.UserProfileResponse;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String username) {
        UserProfileResponse response = userService.profile(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<MessageResponse> update(@Valid @RequestBody UserUpdateRequest req) {
        MessageResponse response = userService.update(req.getUserName(), req);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@Valid @RequestBody UserDeleteRequest req) {
        userService.delete(req.getUserName(), req);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
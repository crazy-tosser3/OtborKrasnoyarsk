package com.example.balloon.controller;

import com.example.balloon.model.dto.user.request.UserDeleteRequest;
import com.example.balloon.model.dto.user.request.UserUpdateRequest;
import com.example.balloon.model.dto.user.response.UserProfileResponse;
import com.example.balloon.model.dto.user.response.UserUpdateResponse;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{username}")
    public UserProfileResponse profile(@PathVariable String username) {
        return userService.profile(username);
    }

    @PostMapping("/update")
    public UserUpdateResponse update(@Valid @RequestBody UserUpdateRequest req) {
        return userService.update(req.getUserName(), req);
    }

    @DeleteMapping("/delete")
    public void delete(@Valid @RequestBody UserDeleteRequest req) {
        userService.delete(req.getUserName(), req);
    }
}
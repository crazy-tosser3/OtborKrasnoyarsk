package com.example.balloon.controller;

import com.example.balloon.model.dto.user.request.UserDeleteRequest;
import com.example.balloon.model.dto.user.request.UserUpdateRequest;
import com.example.balloon.model.dto.user.response.UserProfileResponse;
import com.example.balloon.model.dto.user.response.UserUpdateResponse;
import com.example.balloon.security.UserPrincipal;
import com.example.balloon.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserProfileResponse profile(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.profile(principal.getUsername());
    }

    @PutMapping("/me")
    public UserUpdateResponse update(@AuthenticationPrincipal UserPrincipal principal,
                                     @Valid @RequestBody UserUpdateRequest req) {
        return userService.update(principal.getUsername(), req);
    }

    @DeleteMapping("/me")
    public void delete(@AuthenticationPrincipal UserPrincipal principal,
                       @Valid @RequestBody UserDeleteRequest req) {
        userService.delete(principal.getUsername(), req);
    }
}
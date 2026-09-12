package com.example.balloon.controller;

import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.ChangeRoleRequest;
import com.example.balloon.model.dto.user.UserResponse;
import com.example.balloon.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("username") String userName) {
        return ResponseEntity.ok(adminService.getUser(userName));
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponse> changeRole(@RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(adminService.changeRole(req));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable("username") String userName) {
        adminService.deleteUser(userName);
        return ResponseEntity.ok(new MessageResponse("user deleted"));
    }
}
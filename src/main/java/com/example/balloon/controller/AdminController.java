package com.example.balloon.controller;

import com.example.balloon.model.dto.user.request.ChangeRoleRequest;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.service.AdminService;
import com.example.balloon.service.GameService;
import com.example.balloon.service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final RewardService rewardService;
    private final GameService gameService;

    public AdminController(AdminService adminService, RewardService rewardService, GameService gameService) {
        this.adminService = adminService;
        this.rewardService = rewardService;
        this.gameService = gameService;
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{username}")
    public UserEntity getUser(@PathVariable("username") String userName) {
        return adminService.getUser(userName);
    }

    @PutMapping("/users/role")
    public UserEntity changeRole(@RequestBody ChangeRoleRequest req) {
        return adminService.changeRole(req);
    }

    @DeleteMapping("/users/{username}")
    public Map<String, String> deleteUser(@PathVariable("username") String userName) {
        adminService.deleteUser(userName);
        return Map.of("message", "user deleted");
    }

    @PostMapping("/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public RewardEntity createReward(@RequestBody RewardEntity reward) {
        return rewardService.create(reward);
    }

    @GetMapping("/rewards")
    public List<RewardEntity> getAllRewards() {
        return rewardService.getAll();
    }

    @GetMapping("/games")
    public List<GameHistoryEntity> getAllGames() {
        return gameService.getAdminGames();
    }
}
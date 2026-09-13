package com.example.balloon.controller;

import com.example.balloon.model.dto.game.GameConfig;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.reward.RewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.model.dto.tournament.ActiveTournamentResponse;
import com.example.balloon.model.dto.tournament.CreateTournamentRequest;
import com.example.balloon.model.dto.user.ChangeRoleRequest;
import com.example.balloon.model.dto.user.UserResponse;
import com.example.balloon.service.AdminService;
import com.example.balloon.service.GameConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final GameConfigService gameConfigService;

    public AdminController(AdminService adminService, GameConfigService gameConfigService) {
        this.adminService = adminService;
        this.gameConfigService = gameConfigService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username) {
        return ResponseEntity.ok(adminService.getUser(username));
    }

    @PutMapping("/users/role")
    public ResponseEntity<UserResponse> changeRole(@RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(adminService.changeRole(request));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("message", adminService.deleteUser(username)));
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<ActiveTournamentResponse>> getTournaments() {
        return ResponseEntity.ok(adminService.getTournaments());
    }

    @PostMapping("/tournaments")
    public ResponseEntity<ActiveTournamentResponse> createTournament(@RequestBody CreateTournamentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTournament(request));
    }

    @DeleteMapping("/tournaments/{id}")
    public ResponseEntity<Map<String, String>> deleteTournament(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("message", adminService.deleteTournament(id)));
    }

    @PostMapping("/rewards")
    public ResponseEntity<RewardResponse> createReward(@RequestBody RewardRequest reward) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createReward(reward));
    }

    @GetMapping("/rewards")
    public ResponseEntity<List<RewardResponse>> getRewards() {
        return ResponseEntity.ok(adminService.getRewards());
    }

    @GetMapping("/games")
    public ResponseEntity<List<GameHistoryResponse>> getGames() {
        return ResponseEntity.ok(adminService.getGames());
    }

    @GetMapping("/game_config")
    public ResponseEntity<GameConfig> getGameConfig() {
        return ResponseEntity.ok(gameConfigService.get());
    }

    @PutMapping("/game_config")
    public ResponseEntity<GameConfig> updateGameConfig(@RequestBody GameConfig config) {
        return ResponseEntity.ok(gameConfigService.update(config));
    }
}

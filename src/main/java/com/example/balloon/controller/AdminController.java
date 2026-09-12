package com.example.balloon.controller;

import com.example.balloon.model.dto.common.MessageResponse;
import com.example.balloon.model.dto.user.ChangeRoleRequest;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.model.dto.user.UserResponse;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.service.AdminService;
import com.example.balloon.service.GameService;
import com.example.balloon.service.RewardService;
import com.example.balloon.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final RewardService rewardService;
    private final GameService gameService;
    private final TournamentService tournamentService;

    public AdminController(AdminService adminService, RewardService rewardService,
                           GameService gameService, TournamentService tournamentService) {
        this.adminService = adminService;
        this.rewardService = rewardService;
        this.gameService = gameService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("username") String userName) {
        return ResponseEntity.ok(adminService.getUser(userName));
    }

    @PutMapping("/users/role")
    public ResponseEntity<UserResponse> changeRole(@RequestBody ChangeRoleRequest req) {
        return ResponseEntity.ok(adminService.changeRole(req));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable("username") String userName) {
        adminService.deleteUser(userName);
        return ResponseEntity.ok(new MessageResponse("user deleted"));
    }

    @PostMapping("/rewards")
    public ResponseEntity<RewardResponse> createReward(@RequestBody RewardEntity reward) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rewardService.create(reward));
    }

    @GetMapping("/rewards")
    public ResponseEntity<List<RewardResponse>> getAllRewards() {
        return ResponseEntity.ok(rewardService.getAll());
    }

    @GetMapping("/games")
    public ResponseEntity<List<GameHistoryResponse>> getAllGames() {
        return ResponseEntity.ok(gameService.getAdminGames());
    }

    @PostMapping("/tournaments")
    public ResponseEntity<TournamentResponse> createTournament(@RequestBody TournamentEntity tournament) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tournamentService.create(tournament));
    }

    @GetMapping("/tournaments")
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAll());
    }

    @DeleteMapping("/tournaments/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable String id) {
        tournamentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
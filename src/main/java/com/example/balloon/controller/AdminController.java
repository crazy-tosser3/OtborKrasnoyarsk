package com.example.balloon.controller;

import com.example.balloon.model.dto.*;
import com.example.balloon.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Администрирование")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{username}")
    @Operation(summary = "Получить пользователя", description = "Получить пользователя по username")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username) {
        return ResponseEntity.ok(adminService.getUser(username));
    }

    @PutMapping("/users/role")
    @Operation(summary = "Изменить роль пользователя", description = "Изменение роли пользователя")
    public ResponseEntity<UserResponse> changeRole(@RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(adminService.changeRole(request));
    }

    @DeleteMapping("/users/{username}")
    @Operation(summary = "Удалить пользователя", description = "Удаление пользователя по username")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("message", adminService.deleteUser(username)));
    }

    @GetMapping("/tournaments")
    @Operation(summary = "Список турниров", description = "Активный турнир из Redis")
    public ResponseEntity<List<ActiveTournamentResponse>> getTournaments() {
        return ResponseEntity.ok(adminService.getTournaments());
    }

    @PostMapping("/tournaments")
    @Operation(summary = "Создать турнир", description = "Записывает активный турнир в Redis")
    public ResponseEntity<ActiveTournamentResponse> createTournament(@RequestBody CreateTournamentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTournament(request));
    }

    @DeleteMapping("/tournaments/{id}")
    @Operation(summary = "Удалить турнир", description = "Удаляет активный турнир из Redis либо запись из архива")
    public ResponseEntity<Map<String, String>> deleteTournament(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("message", adminService.deleteTournament(id)));
    }

    @PostMapping("/rewards")
    @Operation(summary = "Создать награду")
    public ResponseEntity<RewardResponse> createReward(@RequestBody RewardRequest reward) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createReward(reward));
    }

    @GetMapping("/rewards")
    @Operation(summary = "Все награды")
    public ResponseEntity<List<RewardResponse>> getRewards() {
        return ResponseEntity.ok(adminService.getRewards());
    }

    @GetMapping("/games")
    @Operation(summary = "История игр", description = "Все сыгранные партии, новые сверху")
    public ResponseEntity<List<GameHistoryResponse>> getGames() {
        return ResponseEntity.ok(adminService.getGames());
    }
}

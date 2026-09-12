package com.example.balloon.controller;

import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
public class AdminGameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameHistoryResponse>> getAllGames() {
        return ResponseEntity.ok(gameService.getAdminGames());
    }
}
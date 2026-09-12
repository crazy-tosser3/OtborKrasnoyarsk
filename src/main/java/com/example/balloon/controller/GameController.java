package com.example.balloon.controller;

import com.example.balloon.model.dto.game.LiveLeaderboardResponse;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.game.LeaderboardEntryResponse;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/games/history/global")
    public ResponseEntity<List<GameHistoryResponse>> getGlobalHistory() {
        return ResponseEntity.ok(gameService.getGlobalHistory());
    }

    @GetMapping("/tournament")
    public ResponseEntity<TournamentResponse> getCurrentTournament() {
        return ResponseEntity.ok(gameService.getCurrentTournament());
    }

    @GetMapping("/tournament/top")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard() {
        return ResponseEntity.ok(gameService.getLeaderboard());
    }

    @GetMapping("/tournament/live")
    public ResponseEntity<LiveLeaderboardResponse> getLiveLeaderboard() {
        return ResponseEntity.ok(gameService.getLiveLeaderboard());
    }
}
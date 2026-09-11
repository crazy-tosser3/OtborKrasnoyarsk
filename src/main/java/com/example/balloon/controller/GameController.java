package com.example.balloon.controller;

import com.example.balloon.model.dto.LiveLeaderboardResponse;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.LeaderboardEntryEntity;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.service.GameService;
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
    public List<GameHistoryEntity> getGlobalHistory() {
        return gameService.getGlobalHistory();
    }

    @GetMapping("/tournament")
    public TournamentEntity getCurrentTournament() {
        return gameService.getCurrentTournament();
    }

    @GetMapping("/tournament/top")
    public List<LeaderboardEntryEntity> getLeaderboard() {
        return gameService.getLeaderboard();
    }

    @GetMapping("/tournament/live")
    public LiveLeaderboardResponse getLiveLeaderboard() {
        return gameService.getLiveLeaderboard();
    }
}
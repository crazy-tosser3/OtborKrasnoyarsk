package com.example.balloon.controller;

import com.example.balloon.model.dto.game.LeaderboardEntryResponse;
import com.example.balloon.model.dto.minigame.FinishMiniGameRequest;
import com.example.balloon.model.dto.minigame.StartMiniGameResponse;
import com.example.balloon.service.MiniGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/minigame")
public class MiniGameController {

    private final MiniGameService miniGameService;

    public MiniGameController(MiniGameService miniGameService) {
        this.miniGameService = miniGameService;
    }

    @PostMapping("/start")
    public StartMiniGameResponse start(Authentication authentication) {
        String userName = authentication.getName();
        return miniGameService.start(userName);
    }

    @PostMapping("/finish")
    public Map<String, Object> finish(@RequestBody FinishMiniGameRequest req) {
        return miniGameService.finish(req);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard() {
        return ResponseEntity.ok(miniGameService.getLeaderboard());
    }
}
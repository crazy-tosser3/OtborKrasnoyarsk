package com.example.balloon.controller;

import com.example.balloon.model.dto.game.LeaderboardEntryResponse;
import com.example.balloon.model.dto.minigame.FinishMiniGameRequest;
import com.example.balloon.model.dto.minigame.StartMiniGameResponse;
import com.example.balloon.service.MiniGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/minigame")
@RequiredArgsConstructor
public class MiniGameController {

    private final MiniGameService miniGameService;

    @PostMapping("/start")
    public ResponseEntity<StartMiniGameResponse> start(@AuthenticationPrincipal String userName) {
        return ResponseEntity.ok(miniGameService.start(userName));
    }

    @PostMapping("/finish")
    public ResponseEntity<Map<String, Object>> finish(@RequestBody FinishMiniGameRequest req) {
        return ResponseEntity.ok(miniGameService.finish(req));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard() {
        return ResponseEntity.ok(miniGameService.getLeaderboard());
    }
}
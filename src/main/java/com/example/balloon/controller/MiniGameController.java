package com.example.balloon.controller;

import com.example.balloon.exception.UnauthorizedException;
import com.example.balloon.model.dto.game.FinishMiniGameRequest;
import com.example.balloon.model.dto.game.FinishMiniGameResponse;
import com.example.balloon.model.dto.leaderboard.MiniGameLeaderboardProjection;
import com.example.balloon.model.dto.game.StartMiniGameResponse;
import com.example.balloon.service.MiniGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/minigame")
@Tag(name = "MiniGame", description = "Мини-игра")
public class MiniGameController {

    private final MiniGameService miniGameService;

    public MiniGameController(MiniGameService miniGameService) {
        this.miniGameService = miniGameService;
    }

    @PostMapping("/start")
    @Operation(summary = "Начать мини-игру",
            description = "Создаёт сессию в Redis и возвращает секрет для подписи результата",
            security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<StartMiniGameResponse> start(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new UnauthorizedException("authorization required");
        }
        return ResponseEntity.ok(miniGameService.start(principal.getName()));
    }

    @PostMapping("/finish")
    @Operation(summary = "Завершить мини-игру",
            description = "Проверяет hash = sha256(session_id + score + secret) и длительность раунда",
            security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<FinishMiniGameResponse> finish(@RequestBody FinishMiniGameRequest request) {
        return ResponseEntity.ok(miniGameService.finish(request));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Лидерборд мини-игры", description = "Лучший результат каждого игрока, до 100 записей")
    public ResponseEntity<List<MiniGameLeaderboardProjection>> getLeaderboard() {
        return ResponseEntity.ok(miniGameService.getLeaderboard());
    }
}

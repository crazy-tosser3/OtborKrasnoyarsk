package com.example.balloon.controller;

import com.example.balloon.model.dto.reward.ClaimRewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@Tag(name = "Rewards", description = "Награды игроков")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    @Operation(summary = "Инвентарь игрока", description = "Список наград пользователя")
    public ResponseEntity<List<RewardResponse>> getRewards(@RequestParam String username) {
        return ResponseEntity.ok(rewardService.getRewards(username));
    }

    @PostMapping("/claim")
    @Operation(summary = "Получить награду", description = "Помечает награду как полученную")
    public ResponseEntity<RewardResponse> claimReward(@RequestBody ClaimRewardRequest request) {
        return ResponseEntity.ok(rewardService.claimReward(request));
    }
}

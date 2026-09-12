package com.example.balloon.controller;

import com.example.balloon.model.dto.reward.ClaimRewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/rewards")
    public ResponseEntity<List<RewardResponse>> getByUserName(@RequestParam("username") String userName) {
        return ResponseEntity.ok(rewardService.getByUserName(userName));
    }

    @PostMapping("/rewards/claim")
    public ResponseEntity<RewardResponse> claimReward(@RequestBody ClaimRewardRequest req) {
        return ResponseEntity.ok(rewardService.claimReward(req));
    }
}
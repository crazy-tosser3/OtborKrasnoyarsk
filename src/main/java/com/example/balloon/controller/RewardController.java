package com.example.balloon.controller;

import com.example.balloon.model.dto.ClaimRewardRequest;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.service.RewardService;
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
    public List<RewardEntity> getByUserName(@RequestParam("username") String userName) {
        return rewardService.getByUserName(userName);
    }

    @PostMapping("/rewards/claim")
    public RewardEntity claimReward(@RequestBody ClaimRewardRequest req) {
        return rewardService.claimReward(req);
    }
}
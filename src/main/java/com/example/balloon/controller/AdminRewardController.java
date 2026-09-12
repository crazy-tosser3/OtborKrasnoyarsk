package com.example.balloon.controller;

import com.example.balloon.model.dto.reward.RewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rewards")
@RequiredArgsConstructor
public class AdminRewardController {

    private final RewardService rewardService;
    private final EntityMapper mapper;

    @PostMapping
    public ResponseEntity<RewardResponse> createReward(@RequestBody RewardRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rewardService.create(mapper.toRewardEntity(req)));
    }

    @GetMapping
    public ResponseEntity<List<RewardResponse>> getAllRewards() {
        return ResponseEntity.ok(rewardService.getAll());
    }
}
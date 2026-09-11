package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.ClaimRewardRequest;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.repository.RewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RewardService {
    private final RewardRepository rewardRepository;

    public RewardService(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Transactional(readOnly = true)
    public List<RewardEntity> getByUserName(String userName) {
        return rewardRepository.findByUserName(userName);
    }

    @Transactional
    public RewardEntity claimReward(ClaimRewardRequest req) {
        RewardEntity reward = rewardRepository.findById(req.getRewardId())
                .orElseThrow(() -> new NotFoundException("reward not found"));
        reward.setClaimed(true);
        return rewardRepository.save(reward);
    }

    @Transactional
    public RewardEntity create(RewardEntity reward) {
        return rewardRepository.save(reward);
    }

    @Transactional(readOnly = true)
    public List<RewardEntity> getAll() {
        return rewardRepository.findAll();
    }
}
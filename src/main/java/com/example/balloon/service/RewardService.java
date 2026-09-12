package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.reward.ClaimRewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.repository.RewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RewardService {
    private final RewardRepository rewardRepository;
    private final EntityMapper mapper;

    public RewardService(RewardRepository rewardRepository, EntityMapper mapper) {
        this.rewardRepository = rewardRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getByUserName(String userName) {
        return mapper.toRewardResponseList(rewardRepository.findByUserName(userName));
    }

    @Transactional
    public RewardResponse claimReward(ClaimRewardRequest req) {
        RewardEntity reward = rewardRepository.findById(req.getRewardId())
                .orElseThrow(() -> new NotFoundException("reward not found"));
        reward.setClaimed(true);
        return mapper.toRewardResponse(rewardRepository.save(reward));
    }

    @Transactional
    public RewardResponse create(RewardEntity reward) {
        return mapper.toRewardResponse(rewardRepository.save(reward));
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getAll() {
        return mapper.toRewardResponseList(rewardRepository.findAll());
    }
}
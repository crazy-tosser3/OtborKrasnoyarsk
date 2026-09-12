package com.example.balloon.model.dto.reward;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClaimRewardRequest {
    @JsonProperty("reward_id")
    private String rewardId;
}

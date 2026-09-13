package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinishMiniGameResponse {
    private String message;
    private Integer score;
    @JsonProperty("reward_created")
    private Boolean rewardCreated;
    @JsonProperty("bonus_granted")
    private Boolean bonusGranted;
    @JsonProperty("session_id")
    private String sessionId;
}

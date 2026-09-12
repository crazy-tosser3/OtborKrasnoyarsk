package com.example.balloon.model.dto.minigame;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
public class FinishMiniGameRequest {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("score")
    private int score;
}
package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FinishMiniGameRequest {
    @JsonProperty("session_id")
    private String sessionId;
    private Integer score;
    /** sha256(session_id + score + secret) — защита от подделки результата */
    private String hash;
}

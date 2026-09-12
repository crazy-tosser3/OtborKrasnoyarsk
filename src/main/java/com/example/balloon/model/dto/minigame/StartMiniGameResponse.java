package com.example.balloon.model.dto.minigame;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartMiniGameResponse {
    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("duration")
    private int duration;
}
package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinishMiniGameRequest {
    private String sessionId;
    private Integer score;
    private String hash;
}

package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FinishMiniGameResponse {
    private String message;
    private Integer score;
    private Boolean rewardCreated;
    private Boolean bonusGranted;
    private String sessionId;
}

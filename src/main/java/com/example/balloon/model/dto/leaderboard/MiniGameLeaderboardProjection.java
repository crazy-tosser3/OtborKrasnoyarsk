package com.example.balloon.model.dto.leaderboard;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public interface MiniGameLeaderboardProjection {
    String getUserName();
    Integer getScore();
}
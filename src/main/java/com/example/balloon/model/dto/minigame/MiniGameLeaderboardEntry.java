package com.example.balloon.model.dto.minigame;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MiniGameLeaderboardEntry {
    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("score")
    private int score;
}
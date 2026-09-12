package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LiveLeaderboardResponse {
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("top")
    private List<LeaderboardEntryResponse> top;
}
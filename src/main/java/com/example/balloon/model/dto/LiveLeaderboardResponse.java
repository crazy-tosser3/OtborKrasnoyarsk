package com.example.balloon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.balloon.model.entity.LeaderboardEntryEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LiveLeaderboardResponse {
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("top")
    private List<LeaderboardEntryEntity> top;
}

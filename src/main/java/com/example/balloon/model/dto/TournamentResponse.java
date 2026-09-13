package com.example.balloon.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentResponse {
    private String id;
    private String name;
    @JsonProperty("started_at")
    private Instant startedAt;
    @JsonProperty("ended_at")
    private Instant endedAt;
    private String winner;
}

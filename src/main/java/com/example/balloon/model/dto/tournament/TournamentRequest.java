package com.example.balloon.model.dto.tournament;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TournamentRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("ends_at")
    private String endsAt;
}
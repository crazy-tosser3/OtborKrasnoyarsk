package com.example.balloon.model.dto.tournament;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveTournamentResponse {
    private String id;
    private String name;

    @JsonProperty("started_at")
    @JsonAlias({"startedAt"})
    private Instant startedAt;

    @JsonProperty("ends_at")
    @JsonAlias({"endsAt", "endedAt", "ended_at"})
    private Instant endsAt;
}

package com.example.balloon.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateTournamentRequest {
    private String name;

    @JsonProperty("startedAt")
    @JsonAlias({"started_at"})
    private Instant startedAt;

    /** в Go-версии поле называлось endedAt, оставляем совместимость с обоими вариантами */
    @JsonProperty("endedAt")
    @JsonAlias({"ends_at", "endsAt", "ended_at"})
    private Instant endsAt;
}

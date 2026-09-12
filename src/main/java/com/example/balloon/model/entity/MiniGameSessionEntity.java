package com.example.balloon.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mini_game_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniGameSessionEntity {

    @Id
    @Column(name = "id")
    @JsonProperty("id")
    private String id;

    @Column(name = "user_name", nullable = false)
    @JsonProperty("user_name")
    private String userName;

    @Column(name = "score", nullable = false)
    @JsonProperty("score")
    private int score;

    @Column(name = "started_at")
    @JsonProperty("started_at")
    private String startedAt;

    @Column(name = "finished", nullable = false)
    @JsonProperty("finished")
    private boolean finished;
}
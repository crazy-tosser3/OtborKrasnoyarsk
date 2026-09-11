package com.example.balloon.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonProperty("id")
    private String id;

    @Column(name = "user_name", nullable = false)
    @JsonProperty("user_name")
    private String userName;

    @Column(name = "score", nullable = false)
    @JsonProperty("score")
    private int score;

    @Column(name = "played_at")
    @JsonProperty("played_at")
    private String playedAt;
}
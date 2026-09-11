package com.example.balloon.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leaderboard")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_name", nullable = false)
    @JsonProperty("user_name")
    private String userName;

    @Column(name = "score", nullable = false)
    @JsonProperty("score")
    private int score;

    public LeaderboardEntryEntity(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }
}
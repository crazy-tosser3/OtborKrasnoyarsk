package com.example.balloon.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String userName;

    @Column(name = "score", nullable = false)
    private int score;

    public LeaderboardEntryEntity(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }
}
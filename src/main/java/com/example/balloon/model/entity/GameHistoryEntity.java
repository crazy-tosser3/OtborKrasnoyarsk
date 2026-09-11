package com.example.balloon.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "game_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "played_at")
    private String playedAt;
}
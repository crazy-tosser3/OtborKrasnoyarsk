package com.example.balloon.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "claimed", nullable = false)
    private boolean claimed;

    @Column(name = "user_name")
    private String userName;
}

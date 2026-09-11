package com.example.balloon.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tournament")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "ends_at")
    private String endsAt;

    public TournamentEntity(String name, String endsAt) {
        this.name = name;
        this.endsAt = endsAt;
    }
}
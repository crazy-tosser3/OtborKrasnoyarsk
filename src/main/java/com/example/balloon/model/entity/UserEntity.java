package com.example.balloon.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;
}
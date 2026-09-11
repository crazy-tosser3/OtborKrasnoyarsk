package com.example.balloon.model.entity;

import com.example.balloon.model.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private String id;

    @Column(name = "user_name", nullable = false, unique = true)
    @JsonProperty("user_name")
    private String userName;

    @Column(name = "user_email", unique = true)
    @JsonProperty("user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    @JsonProperty("user_role")
    private RoleEnum userRole;

    @Column(name = "password_hash", nullable = false)
    @JsonProperty("password_hash")
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    @JsonProperty("salt")
    private String salt;
}
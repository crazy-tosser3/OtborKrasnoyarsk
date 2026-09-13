package com.example.balloon.repository;

import com.example.balloon.model.entity.MiniGameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MiniGameSessionRepository extends JpaRepository<MiniGameSessionEntity, String> {
}

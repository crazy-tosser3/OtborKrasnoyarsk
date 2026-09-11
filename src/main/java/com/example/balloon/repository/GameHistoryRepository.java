package com.example.balloon.repository;

import com.example.balloon.model.entity.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, String> {
}

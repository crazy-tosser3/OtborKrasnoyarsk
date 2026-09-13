package com.example.balloon.repository;

import com.example.balloon.model.entity.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, String> {

    /** История игр, новые сверху (в Go — ORDER BY played_at DESC). */
    List<GameHistoryEntity> findAllByOrderByPlayedAtDesc();

    List<GameHistoryEntity> findByUserNameOrderByPlayedAtDesc(String userName);
}

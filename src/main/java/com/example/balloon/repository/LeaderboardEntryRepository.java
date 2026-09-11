package com.example.balloon.repository;

import com.example.balloon.model.entity.LeaderboardEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntryEntity, String> {
}

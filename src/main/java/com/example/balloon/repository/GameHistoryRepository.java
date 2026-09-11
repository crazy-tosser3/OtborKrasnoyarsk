package com.example.balloon.repository;

import com.example.balloon.model.entity.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, String> {

    @Query(value = "SELECT user_name AS userName, " +
            "MAX(score) AS score FROM game_histories " +
            "GROUP BY user_name ORDER BY score DESC LIMIT 100", nativeQuery = true)
    List<LeaderboardProjection> getLeaderboard();
}
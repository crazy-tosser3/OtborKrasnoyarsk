package com.example.balloon.repository;

import com.example.balloon.model.dto.leaderboard.MiniGameLeaderboardProjection;
import com.example.balloon.model.entity.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, String> {
    List<GameHistoryEntity> findAllByOrderByPlayedAtDesc();

    List<GameHistoryEntity> findByUserNameOrderByPlayedAtDesc(String userName);

    long countByUserNameAndPlayedAtGreaterThanEqual(String userName, String playedAt);

    @Query(value = """
            SELECT
                user_name AS userName,
                MAX(score) AS score
            FROM game_histories
            WHERE is_success = true
            GROUP BY user_name
            ORDER BY score DESC
            LIMIT 100
            """,
            nativeQuery = true)
    List<MiniGameLeaderboardProjection> findLeaderboard();
}

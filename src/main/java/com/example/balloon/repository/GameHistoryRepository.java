package com.example.balloon.repository;

import com.example.balloon.model.dto.leaderboard.MiniGameLeaderboardProjection;
import com.example.balloon.model.entity.GameHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistoryEntity, String> {

    /** История игр, новые сверху (в Go — ORDER BY played_at DESC). */
    List<GameHistoryEntity> findAllByOrderByPlayedAtDesc();

    List<GameHistoryEntity> findByUserNameOrderByPlayedAtDesc(String userName);

    /** Сколько игр у пользователя начиная с момента; played_at хранится строкой RFC3339. */
    long countByUserNameAndPlayedAtGreaterThanEqual(String userName, String playedAt);

    /** Лучший результат каждого игрока по успешным играм. */
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

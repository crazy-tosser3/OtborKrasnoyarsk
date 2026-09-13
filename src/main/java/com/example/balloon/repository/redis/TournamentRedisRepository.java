package com.example.balloon.repository.redis;

import com.example.balloon.model.dto.tournament.ActiveTournamentResponse;
import com.example.balloon.model.dto.leaderboard.LeaderboardEntryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Активный турнир и его лидерборд живут в Redis (как в Go-версии):
 *   tournament:active                   -> JSON активного турнира
 *   tournament:{id}:leaderboard         -> ZSET username -> score
 */
@Repository
@Slf4j
public class TournamentRedisRepository {

    private static final String ACTIVE_KEY = "tournament:active";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public TournamentRedisRepository(StringRedisTemplate redis,
                                     @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private String leaderboardKey(String tournamentId) {
        return "tournament:" + tournamentId + ":leaderboard";
    }

    public Optional<ActiveTournamentResponse> getActiveTournament() {
        try {
            String data = redis.opsForValue().get(ACTIVE_KEY);
            if (data == null || data.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(data, ActiveTournamentResponse.class));
        } catch (Exception e) {
            log.error("Не удалось прочитать активный турнир из Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void setActiveTournament(ActiveTournamentResponse tournament) {
        try {
            redis.opsForValue().set(ACTIVE_KEY, objectMapper.writeValueAsString(tournament));
        } catch (Exception e) {
            throw new IllegalStateException("cannot store active tournament: " + e.getMessage(), e);
        }
    }

    public void deleteActiveTournament() {
        redis.delete(ACTIVE_KEY);
    }

    /** ZINCRBY tournament:{id}:leaderboard score username */
    public void addTournamentScore(String tournamentId, String username, int score) {
        redis.opsForZSet().incrementScore(leaderboardKey(tournamentId), username, score);
    }

    /** ZREVRANGE tournament:{id}:leaderboard 0 limit-1 WITHSCORES */
    public List<LeaderboardEntryResponse> getTournamentTop(String tournamentId, long limit) {
        Set<ZSetOperations.TypedTuple<String>> top =
                redis.opsForZSet().reverseRangeWithScores(leaderboardKey(tournamentId), 0, limit - 1);

        if (top == null) {
            top = new LinkedHashSet<>();
        }

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>(top.size());
        for (ZSetOperations.TypedTuple<String> entry : top) {
            if (entry.getValue() == null) {
                continue;
            }
            int score = entry.getScore() == null ? 0 : entry.getScore().intValue();
            leaderboard.add(new LeaderboardEntryResponse(entry.getValue(), score));
        }
        return leaderboard;
    }

    public void deleteLeaderboard(String tournamentId) {
        redis.delete(leaderboardKey(tournamentId));
    }
}

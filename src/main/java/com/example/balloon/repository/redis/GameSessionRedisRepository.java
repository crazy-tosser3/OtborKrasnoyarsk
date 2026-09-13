package com.example.balloon.repository.redis;

import com.example.balloon.model.dto.ActiveGameSessionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Активные сессии мини-игры хранятся в Redis с TTL 30 минут:
 *   session:{id} -> JSON сессии (вместе с секретом для проверки хеша)
 */
@Repository
@Slf4j
public class GameSessionRedisRepository {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public GameSessionRedisRepository(StringRedisTemplate redis,
                                      @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private String key(String sessionId) {
        return "session:" + sessionId;
    }

    public void save(ActiveGameSessionDTO session) {
        try {
            redis.opsForValue().set(key(session.getId()), objectMapper.writeValueAsString(session), TTL);
        } catch (Exception e) {
            throw new IllegalStateException("cannot store game session: " + e.getMessage(), e);
        }
    }

    public Optional<ActiveGameSessionDTO> find(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        try {
            String data = redis.opsForValue().get(key(sessionId));
            if (data == null || data.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(data, ActiveGameSessionDTO.class));
        } catch (Exception e) {
            log.error("Не удалось прочитать сессию {} из Redis: {}", sessionId, e.getMessage());
            return Optional.empty();
        }
    }

    public void delete(String sessionId) {
        redis.delete(key(sessionId));
    }
}

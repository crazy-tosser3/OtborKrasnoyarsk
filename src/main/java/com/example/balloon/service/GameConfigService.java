package com.example.balloon.service;

import com.example.balloon.exception.BadRequestException;
import com.example.balloon.model.dto.game.GameConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameConfigService {

    private static final String REDIS_KEY = "game:config";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public GameConfigService(StringRedisTemplate redis,
                             @Qualifier("redisObjectMapper") ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public GameConfig get() {
        String data = redis.opsForValue().get(REDIS_KEY);
        if (data == null) {
            return GameConfig.defaults();
        }
        try {
            return objectMapper.readerForUpdating(GameConfig.defaults()).readValue(data);
        } catch (Exception e) {
            throw new IllegalStateException("cannot read game config: " + e.getMessage(), e);
        }
    }

    public GameConfig update(GameConfig config) {
        validate(config);
        try {
            redis.opsForValue().set(REDIS_KEY, objectMapper.writeValueAsString(config));
        } catch (Exception e) {
            throw new IllegalStateException("cannot store game config: " + e.getMessage(), e);
        }
        return config;
    }

    private void validate(GameConfig cfg) {
        if (cfg.getGameDuration() <= 0) {
            throw new BadRequestException("game_duration must be positive");
        }
        if (cfg.getMinPlayTime() < 0) {
            throw new BadRequestException("min_play_time must not be negative");
        }
        if (cfg.getMaxScore() < 0) {
            throw new BadRequestException("max_score must not be negative");
        }
        if (cfg.getBonusEvery() < 0) {
            throw new BadRequestException("bonus_every must not be negative");
        }
        if (cfg.getBonusScore() < 0) {
            throw new BadRequestException("bonus_score must not be negative");
        }
        if (cfg.getMaxGamesPerDay() < 0) {
            throw new BadRequestException("max_games_per_day must not be negative");
        }
        if (cfg.getScoreMultiplier() < 0) {
            throw new BadRequestException("score_multiplier must not be negative");
        }
        if (cfg.getRewardChance() < 0 || cfg.getRewardChance() > 1) {
            throw new BadRequestException("reward_chance must be between 0 and 1");
        }
        if (cfg.getBonusRewardChance() < 0 || cfg.getBonusRewardChance() > 1) {
            throw new BadRequestException("bonus_reward_chance must be between 0 and 1");
        }
    }
}

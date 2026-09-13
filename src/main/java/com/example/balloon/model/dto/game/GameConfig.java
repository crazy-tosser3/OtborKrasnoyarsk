package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameConfig {
    private int gameDuration;
    private int maxScore;
    private int minPlayTime;
    private String rewardName;
    private int bonusEvery;
    private int bonusScore;
    private double rewardChance;
    private double bonusRewardChance;
    private boolean enableRewards;
    private boolean enableBonuses;
    private boolean allowReplay;
    private int maxGamesPerDay;
    private double scoreMultiplier;

    public static GameConfig defaults() {
        GameConfig cfg = new GameConfig();
        cfg.setGameDuration(30);
        cfg.setMaxScore(0);
        cfg.setMinPlayTime(5);
        cfg.setRewardName("Супер награда");
        cfg.setBonusEvery(4);
        cfg.setBonusScore(500);
        cfg.setRewardChance(1);
        cfg.setBonusRewardChance(1);
        cfg.setEnableRewards(true);
        cfg.setEnableBonuses(true);
        cfg.setAllowReplay(true);
        cfg.setMaxGamesPerDay(0);
        cfg.setScoreMultiplier(1);
        return cfg;
    }
}

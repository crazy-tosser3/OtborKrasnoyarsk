package com.example.balloon.model.dto.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
public class GameConfig {

    @JsonProperty("game_duration")
    private int gameDuration;

    @JsonProperty("max_score")
    private int maxScore;

    @JsonProperty("min_play_time")
    private int minPlayTime;

    @JsonProperty("reward_name")
    private String rewardName;

    @JsonProperty("bonus_every")
    private int bonusEvery;

    @JsonProperty("bonus_score")
    private int bonusScore;

    @JsonProperty("reward_chance")
    private double rewardChance;

    @JsonProperty("bonus_reward_chance")
    private double bonusRewardChance;

    @JsonProperty("enable_rewards")
    private boolean enableRewards;

    @JsonProperty("enable_bonuses")
    private boolean enableBonuses;

    @JsonProperty("allow_replay")
    private boolean allowReplay;

    @JsonProperty("max_games_per_day")
    private int maxGamesPerDay;

    @JsonProperty("score_multiplier")
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

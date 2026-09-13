package models

import "time"

type LeaderboardEntry struct {
	UserName string `json:"user_name"`
	Score    int    `json:"score"`
}

type GameHistory struct {
	ID        string `gorm:"primaryKey" json:"id"`
	UserName  string `json:"user_name"`
	Score     int    `json:"score"`
	PlayedAt  string `json:"played_at"`
	IsSuccess bool   `json:"is_succes"`
}

type Reward struct {
	ID       string `gorm:"primaryKey" json:"id"`
	Name     string `json:"name"`
	Claimed  bool   `json:"claimed"`
	UserName string `json:"user_name"`
}

type MiniGameSession struct {
	ID        string `gorm:"primaryKey" json:"id"`
	UserName  string `json:"user_name"`
	Score     int    `json:"score"`
	StartedAt string `json:"started_at"`
	Finished  bool   `json:"finished"`

	ServerSeed string `json:"-"`
}

type Tournament struct {
	ID        uint `gorm:"primaryKey"`
	Name      string
	StartedAt time.Time
	EndedAt   time.Time
	Winner    *string
}

type ActiveGameSession struct {
	ID         string `json:"id"`
	UserName   string `json:"user_name"`
	StartedAt  int64  `json:"started_at"`
	ServerSeed string `json:"server_seed"`
	Secret     string `json:"secret"`
}

type StartMiniGameResponse struct {
	SessionID string `json:"session_id"`
	Duration  int    `json:"duration"`
	Secret    string `json:"secret"`
}

type Config struct {
	GameDuration int `json:"game_duration"`
	MaxScore int `json:"max_score"`
	MinPlayTime int `json:"min_play_time"`
	RewardName string `json:"reward_name"`
	BonusEvery int `json:"bonus_every"`
	BonusScore int `json:"bonus_score"`
	RewardChance float64 `json:"reward_chance"`
	BonusRewardChance float64 `json:"bonus_reward_chance"`
	EnableRewards bool `json:"enable_rewards"`
	EnableBonuses bool `json:"enable_bonuses"`
	AllowReplay bool `json:"allow_replay"`
	MaxGamesPerDay int `json:"max_games_per_day"`
	ScoreMultiplier float64 `json:"score_multiplier"`
}
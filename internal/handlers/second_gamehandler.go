package handlers

import (
	"Otbor/internal/database"
	"Otbor/internal/models"
	"Otbor/internal/service"
	"math/rand"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type FinishMiniGameRequest struct {
	SessionID string `json:"session_id"`
	Score     int    `json:"score"`
	Hash      string `json:"hash"`
}

type MiniGameLeaderboardEntry struct {
	UserName string `json:"user_name"`
	Score    int    `json:"score"`
}

// StartMiniGame godoc
// @Summary Начать мини-игру
// @Tags MiniGame
// @Produce json
// @Security BearerAuth
// @Success 200 {object} models.StartMiniGameResponse
// @Router /api/minigame/start [post]
func StartMiniGame(c *gin.Context) {
	username := c.GetString("username")

	cfg, err := service.GetGameConfig()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	if !cfg.AllowReplay || cfg.MaxGamesPerDay > 0 {
		playedToday, err := countTodaysGames(username)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{
				"error": err.Error(),
			})
			return
		}

		if !cfg.AllowReplay && playedToday > 0 {
			c.JSON(http.StatusForbidden, gin.H{
				"error": "replay is disabled, you already played today",
			})
			return
		}

		if cfg.MaxGamesPerDay > 0 && playedToday >= int64(cfg.MaxGamesPerDay) {
			c.JSON(http.StatusForbidden, gin.H{
				"error": "daily games limit reached",
			})
			return
		}
	}

	session := models.ActiveGameSession{
		ID:         uuid.NewString(),
		UserName:   username,
		StartedAt:  time.Now().Unix(),
		ServerSeed: uuid.NewString(),
		Secret:     uuid.NewString(),
	}

	if err := database.SaveGameSession(session); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, models.StartMiniGameResponse{
		SessionID: session.ID,
		Duration:  cfg.GameDuration,
		Secret:    session.Secret,
	})
}

// countTodaysGames считает, сколько игр пользователь уже сыграл
// сегодня (по UTC-суткам) — используется для AllowReplay/MaxGamesPerDay.
func countTodaysGames(username string) (int64, error) {
	startOfDay := time.Now().UTC().Truncate(24 * time.Hour).Format(time.RFC3339)

	var count int64

	err := database.DB.
		Model(&models.GameHistory{}).
		Where("user_name = ? AND played_at >= ?", username, startOfDay).
		Count(&count).Error

	return count, err
}

// FinishMiniGame godoc
// @Summary Завершить мини-игру
// @Tags MiniGame
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body FinishMiniGameRequest true "Game Result"
// @Success 200 {object} map[string]string
// @Router /api/minigame/finish [post]
func FinishMiniGame(c *gin.Context) {
	var req FinishMiniGameRequest

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid request",
		})
		return
	}

	cfg, err := service.GetGameConfig()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	session, err := database.GetGameSession(req.SessionID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "session not found",
		})
		return
	}

	expectedHash := service.BuildGameHash(
		req.SessionID,
		req.Score,
		session.Secret,
	)

	if expectedHash != req.Hash {
		c.JSON(http.StatusForbidden, gin.H{
			"error": "invalid hash",
		})
		return
	}

	duration := time.Now().Unix() - session.StartedAt

	if duration < int64(cfg.MinPlayTime) {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "suspicious result",
		})
		return
	}

	finalScore := req.Score

	if cfg.ScoreMultiplier > 0 {
		finalScore = int(float64(finalScore) * cfg.ScoreMultiplier)
	}

	if cfg.MaxScore > 0 && finalScore > cfg.MaxScore {
		finalScore = cfg.MaxScore
	}

	history := models.GameHistory{
		ID:        uuid.NewString(),
		UserName:  session.UserName,
		Score:     finalScore,
		PlayedAt:  time.Now().Format(time.RFC3339),
		IsSuccess: true,
	}

	if err := database.DB.Create(&history).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	if err := database.DeleteGameSession(req.SessionID); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	var rewardCreated bool
	var bonusGranted bool

	// Вероятность получить награду задается конфигом (rewardChance,
	// "подкрут"): 1.0 — награда всегда, 0.0 — никогда, промежуточные
	// значения — соответствующий шанс на каждое завершение игры.
	if cfg.EnableRewards && rand.Float64() < cfg.RewardChance {
		reward := models.Reward{
			ID:       uuid.NewString(),
			Name:     cfg.RewardName,
			Claimed:  true,
			UserName: session.UserName,
		}

		if err := database.DB.Create(&reward).Error; err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{
				"error": err.Error(),
			})
			return
		}

		rewardCreated = true
	}

	if cfg.EnableBonuses && cfg.BonusEvery > 0 {
		var rewardsCount int64

		if err := database.DB.
			Model(&models.Reward{}).
			Where("user_name = ?", session.UserName).
			Count(&rewardsCount).Error; err != nil {

			c.JSON(http.StatusInternalServerError, gin.H{
				"error": err.Error(),
			})
			return
		}

		isBonusMilestone := rewardsCount > 0 && rewardsCount%int64(cfg.BonusEvery) == 0

		if isBonusMilestone && rand.Float64() < cfg.BonusRewardChance {

			bonusHistory := models.GameHistory{
				ID:        uuid.NewString(),
				UserName:  session.UserName,
				Score:     cfg.BonusScore,
				PlayedAt:  time.Now().Format(time.RFC3339),
				IsSuccess: true,
			}

			if err := database.DB.Create(&bonusHistory).Error; err != nil {
				c.JSON(http.StatusInternalServerError, gin.H{
					"error": err.Error(),
				})
				return
			}

			bonusGranted = true
		}
	}

	c.JSON(http.StatusOK, gin.H{
		"message":        "game finished",
		"score":          finalScore,
		"reward_created": rewardCreated,
		"bonus_granted":  bonusGranted,
	})
}

// GetMiniGameLeaderboard godoc
// @Summary Лидерборд мини-игры
// @Tags MiniGame
// @Produce json
// @Success 200 {array} MiniGameLeaderboardEntry
// @Router /api/minigame/leaderboard [get]
func GetMiniGameLeaderboard(c *gin.Context) {

	leaderboard := []MiniGameLeaderboardEntry{}

	err := database.DB.Raw(`
		SELECT
			user_name,
			MAX(score) AS score
		FROM game_histories
		WHERE is_success = true
		GROUP BY user_name
		ORDER BY score DESC
		LIMIT 100
	`).Scan(&leaderboard).Error

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, leaderboard)
}

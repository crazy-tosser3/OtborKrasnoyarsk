package service

import (
	"Otbor/internal/database"
	"Otbor/internal/models"
	"encoding/json"
	"errors"

	"github.com/redis/go-redis/v9"
)

const gameConfigRedisKey = "game:config"

// DefaultGameConfig возвращает конфиг "по умолчанию" — он воспроизводит
// поведение, которое раньше было зашито в код напрямую (хардкод),
// чтобы до первой настройки через админку игра работала как прежде.
func DefaultGameConfig() models.Config {
	return models.Config{
		GameDuration:      30,
		MaxScore:          0, // 0 = без ограничения
		MinPlayTime:       5,
		RewardName:        "Супер награда",
		BonusEvery:        4,
		BonusScore:        500,
		RewardChance:      1, // 100% — награда выдается всегда, как раньше
		BonusRewardChance: 1,
		EnableRewards:     true,
		EnableBonuses:     true,
		AllowReplay:       true,
		MaxGamesPerDay:    0, // 0 = без ограничения
		ScoreMultiplier:   1,
	}
}

// GetGameConfig читает текущий конфиг игры из Redis. Если конфиг еще
// ни разу не сохранялся, возвращает DefaultGameConfig().
func GetGameConfig() (models.Config, error) {
	data, err := database.RDB.Get(database.Ctx, gameConfigRedisKey).Result()

	if errors.Is(err, redis.Nil) {
		return DefaultGameConfig(), nil
	}

	if err != nil {
		return models.Config{}, err
	}

	// Отталкиваемся от дефолтов, чтобы поля, не сохраненные в старой
	// версии конфига (при добавлении новых полей в будущем), не
	// схлопывались в нулевые значения.
	cfg := DefaultGameConfig()

	if err := json.Unmarshal([]byte(data), &cfg); err != nil {
		return models.Config{}, err
	}

	return cfg, nil
}

// SetGameConfig сохраняет конфиг игры в Redis целиком.
func SetGameConfig(cfg models.Config) error {
	data, err := json.Marshal(cfg)
	if err != nil {
		return err
	}

	return database.RDB.Set(
		database.Ctx,
		gameConfigRedisKey,
		data,
		0,
	).Err()
}

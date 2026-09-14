package service

import (
	"Otbor/internal/database"
	"Otbor/internal/models"
	"encoding/json"
	"time"
)

type ActiveTournament struct {
	ID        string    `json:"id"`
	Name      string    `json:"name"`
	StartedAt time.Time `json:"started_at"`
	EndsAt    time.Time `json:"ends_at"`
}

func GetActiveTournament() (*ActiveTournament, error) {
	data, err := database.RDB.Get(
		database.Ctx,
		"tournament:active",
	).Result()

	if err != nil {
		return nil, err
	}

	var tournament ActiveTournament

	if err := json.Unmarshal([]byte(data), &tournament); err != nil {
		return nil, err
	}

	return &tournament, nil
}

func SetActiveTournament(tournament ActiveTournament) error {
	data, err := json.Marshal(tournament)
	if err != nil {
		return err
	}

	if err := database.RDB.Set(
		database.Ctx,
		"tournament:active",
		data,
		0,
	).Err(); err != nil {
		return err
	}

	// Отдельно создавать лидерборд турнира не нужно: ключ сортированного
	// множества "tournament:<id>:leaderboard" появится сам при первом
	// начислении очков через AddTournamentScore (ZIncrBy создает ключ,
	// если он еще не существует). Раньше здесь вызывался ZAdd без единого
	// участника, из-за чего Redis возвращал ошибку "wrong number of
	// arguments" и создание турнира падало целиком.
	return nil
}

func TournamentWatcher() {
	for {
		active, err := GetActiveTournament()
		if err != nil {
			time.Sleep(10 * time.Second)
			continue
		}

		if time.Now().Before(active.EndsAt) {
			time.Sleep(10 * time.Second)
			continue
		}

		var winner *string

		top, err := database.GetTournamentTop(active.ID, 1)
		if err == nil && len(top) > 0 {
			if name, ok := top[0].Member.(string); ok {
				winner = &name
			}
		}

		tournament := models.Tournament{
			Name:      active.Name,
			StartedAt: active.StartedAt,
			EndedAt:   active.EndsAt,
			Winner:    winner,
		}

		if err := database.DB.Create(&tournament).Error; err != nil {
			time.Sleep(10 * time.Second)
			continue
		}

		_ = database.RDB.Del(
			database.Ctx,
			"tournament:active",
			"tournament:"+active.ID+":leaderboard",
		).Err()

		time.Sleep(10 * time.Second)
	}
}

package service

import (
	"Otbor/internal/database"
	"Otbor/internal/models"
	"net/http"

	"github.com/gin-gonic/gin"
)

// GetGlobalHistory godoc
// @Summary История всех игр
// @Tags Games
// @Produce json
// @Success 200 {array} models.GameHistory
// @Router /api/games/history/global [get]
func GetGlobalHistory(c *gin.Context) {
	var history []models.GameHistory

	if err := database.DB.
		Order("played_at desc").
		Find(&history).Error; err != nil {

		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	c.JSON(http.StatusOK, history)
}

// GetTournament godoc
// @Summary Текущий турнир
// @Tags Tournament
// @Produce json
// @Success 200 {object} models.Tournament
// @Router /api/tournament [get]
func GetTournaments(c *gin.Context) {
	var history []models.Tournament

	if err := database.DB.
		Order("ended_at DESC").
		Find(&history).Error; err != nil {

		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	active, _ := GetActiveTournament()

	c.JSON(http.StatusOK, gin.H{
		"active":  active,
		"history": history,
	})
}

// GetTournamentTop godoc
// @Summary Топ игроков турнира
// @Tags Tournament
// @Produce json
// @Success 200 {array} models.LeaderboardEntry
// @Router /api/tournament/top [get]
func GetLeaderBoard(c *gin.Context) {
	tournament, err := GetActiveTournament()

	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{
			"error": "no active tournament",
		})
		return
	}

	top, err := database.GetTournamentTop(
		tournament.ID,
		100,
	)

	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{
			"error": err.Error(),
		})
		return
	}

	leaderboard := make([]models.LeaderboardEntry, 0, len(top))

	for _, player := range top {
		username, ok := player.Member.(string)
		if !ok {
			continue
		}

		leaderboard = append(
			leaderboard,
			models.LeaderboardEntry{
				UserName: username,
				Score:    int(player.Score),
			},
		)
	}

	c.JSON(http.StatusOK, leaderboard)
}

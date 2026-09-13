package com.example.balloon.controller;

import com.example.balloon.model.dto.tournament.ActiveTournamentResponse;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.leaderboard.LeaderboardEntryResponse;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.service.TournamentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Tournament", description = "Турниры и лидерборды")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/tournament")
    @Operation(summary = "Текущий турнир",
            description = "Активный турнир из Redis: массив с одним элементом либо пустой массив")
    public ResponseEntity<List<ActiveTournamentResponse>> getTournaments() {
        return ResponseEntity.ok(tournamentService.getActiveTournaments());
    }

    @GetMapping("/latest_tournaments")
    @Operation(summary = "Список турниров", description = "Архив завершённых турниров")
    public ResponseEntity<List<TournamentResponse>> getLatestTournaments() {
        return ResponseEntity.ok(tournamentService.getLatestTournaments());
    }

    @GetMapping("/tournament/top")
    @Operation(summary = "Топ игроков турнира", description = "Лидерборд активного турнира, до 100 игроков")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderBoard() {
        return ResponseEntity.ok(tournamentService.getLeaderboard());
    }

    @GetMapping("/games/history/global")
    @Operation(summary = "История всех игр", description = "Глобальная история игр, новые сверху")
    public ResponseEntity<List<GameHistoryResponse>> getGlobalHistory() {
        return ResponseEntity.ok(tournamentService.getGlobalHistory());
    }
}

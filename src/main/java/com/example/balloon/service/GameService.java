package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.LiveLeaderboardResponse;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.LeaderboardEntryEntity;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.repository.GameHistoryRepository;
import com.example.balloon.repository.LeaderboardProjection;
import com.example.balloon.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
    private final GameHistoryRepository gameHistoryRepository;
    private final TournamentRepository tournamentRepository;

    public GameService(GameHistoryRepository gameHistoryRepository, TournamentRepository tournamentRepository) {
        this.gameHistoryRepository = gameHistoryRepository;
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional(readOnly = true)
    public List<GameHistoryEntity> getGlobalHistory() {
        List<GameHistoryEntity> all = gameHistoryRepository.findAll();
        all.sort((a, b) -> {
            if (a.getPlayedAt() == null || b.getPlayedAt() == null) return 0;
            return b.getPlayedAt().compareTo(a.getPlayedAt());
        });
        return all;
    }

    @Transactional(readOnly = true)
    public TournamentEntity getCurrentTournament() {
        List<TournamentEntity> all = tournamentRepository.findAll();
        if (all.isEmpty()) throw new NotFoundException("tournament not found");
        return all.get(all.size() - 1);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryEntity> getLeaderboard() {
        List<LeaderboardProjection> rows = gameHistoryRepository.getLeaderboard();
        List<LeaderboardEntryEntity> res = new ArrayList<>();
        for (LeaderboardProjection row : rows) {
            res.add(new LeaderboardEntryEntity(row.getUserName(), row.getScore() != null ? row.getScore().intValue() : 0));
        }
        return res;
    }

    @Transactional(readOnly = true)
    public LiveLeaderboardResponse getLiveLeaderboard() {
        return new LiveLeaderboardResponse(
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                getLeaderboard()
        );
    }

    @Transactional(readOnly = true)
    public List<GameHistoryEntity> getAdminGames() {
        List<GameHistoryEntity> all = gameHistoryRepository.findAll();
        all.sort((a, b) -> {
            if (a.getPlayedAt() == null || b.getPlayedAt() == null) return 0;
            return b.getPlayedAt().compareTo(a.getPlayedAt());
        });
        return all;
    }
}
package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.game.LiveLeaderboardResponse;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.game.LeaderboardEntryResponse;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.LeaderboardEntryEntity;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.model.mapper.EntityMapper;
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
    private final EntityMapper mapper;

    public GameService(GameHistoryRepository gameHistoryRepository,
                       TournamentRepository tournamentRepository,
                       EntityMapper mapper) {
        this.gameHistoryRepository = gameHistoryRepository;
        this.tournamentRepository = tournamentRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getGlobalHistory() {
        List<GameHistoryEntity> all = gameHistoryRepository.findAll();
        all.sort((a, b) -> {
            if (a.getPlayedAt() == null || b.getPlayedAt() == null) return 0;
            return b.getPlayedAt().compareTo(a.getPlayedAt());
        });
        return mapper.toGameHistoryResponseList(all);
    }

    @Transactional(readOnly = true)
    public TournamentResponse getCurrentTournament() {
        List<TournamentEntity> all = tournamentRepository.findAll();
        if (all.isEmpty()) throw new NotFoundException("tournament not found");
        return mapper.toTournamentResponse(all.get(all.size() - 1));
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        List<LeaderboardProjection> rows = gameHistoryRepository.getLeaderboard();
        List<LeaderboardEntryEntity> res = new ArrayList<>();
        for (LeaderboardProjection row : rows) {
            res.add(new LeaderboardEntryEntity(row.getUserName(), row.getScore() != null ? row.getScore().intValue() : 0));
        }
        return mapper.toLeaderboardEntryResponseList(res);
    }

    @Transactional(readOnly = true)
    public LiveLeaderboardResponse getLiveLeaderboard() {
        return new LiveLeaderboardResponse(
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                getLeaderboard()
        );
    }

    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getAdminGames() {
        return getGlobalHistory();
    }
}
package com.example.balloon.service;

import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.tournament.ActiveTournamentResponse;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.leaderboard.LeaderboardEntryResponse;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.repository.GameHistoryRepository;
import com.example.balloon.repository.TournamentRepository;
import com.example.balloon.repository.redis.TournamentRedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentService {

    private static final long LEADERBOARD_LIMIT = 100;

    private final TournamentRepository tournamentRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final TournamentRedisRepository tournamentRedis;
    private final EntityMapper mapper;

    public TournamentService(TournamentRepository tournamentRepository,
                             GameHistoryRepository gameHistoryRepository,
                             TournamentRedisRepository tournamentRedis,
                             EntityMapper mapper) {
        this.tournamentRepository = tournamentRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.tournamentRedis = tournamentRedis;
        this.mapper = mapper;
    }

    public List<ActiveTournamentResponse> getActiveTournaments() {
        return tournamentRedis.getActiveTournament()
                .map(List::of)
                .orElseGet(List::of);
    }

    public ActiveTournamentResponse getActiveTournament() {
        return tournamentRedis.getActiveTournament()
                .orElseThrow(() -> new NotFoundException("no active tournament"));
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> getLatestTournaments() {
        return mapper.toTournamentResponse(tournamentRepository.findAll());
    }

    public List<LeaderboardEntryResponse> getLeaderboard() {
        ActiveTournamentResponse tournament = getActiveTournament();
        return tournamentRedis.getTournamentTop(tournament.getId(), LEADERBOARD_LIMIT);
    }

    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getGlobalHistory() {
        return mapper.toGameHistoryResponseList(gameHistoryRepository.findAllByOrderByPlayedAtDesc());
    }
}

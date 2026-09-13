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

    /** Размер лидерборда турнира. */
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

    /**
     * Активный турнир лежит в Redis. Как и в Go-версии, отдаём массив:
     * либо с единственным турниром, либо пустой.
     */
    public List<ActiveTournamentResponse> getActiveTournaments() {
        return tournamentRedis.getActiveTournament()
                .map(List::of)
                .orElseGet(List::of);
    }

    /** Единственный активный турнир; 404, если турнира нет. */
    public ActiveTournamentResponse getActiveTournament() {
        return tournamentRedis.getActiveTournament()
                .orElseThrow(() -> new NotFoundException("no active tournament"));
    }

    /** Архив завершённых турниров из Postgres. */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getLatestTournaments() {
        return mapper.toTournamentResponse(tournamentRepository.findAll());
    }

    /** Топ игроков активного турнира из ZSET Redis. */
    public List<LeaderboardEntryResponse> getLeaderboard() {
        ActiveTournamentResponse tournament = getActiveTournament();
        return tournamentRedis.getTournamentTop(tournament.getId(), LEADERBOARD_LIMIT);
    }

    /** Глобальная история всех игр, новые сверху. */
    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getGlobalHistory() {
        return mapper.toGameHistoryResponseList(gameHistoryRepository.findAllByOrderByPlayedAtDesc());
    }

    @Transactional
    public void delete(String id) {
        tournamentRepository.deleteById(id);
    }
}

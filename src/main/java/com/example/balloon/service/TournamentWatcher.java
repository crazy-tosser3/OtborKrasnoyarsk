package com.example.balloon.service;

import com.example.balloon.model.dto.ActiveTournamentResponse;
import com.example.balloon.model.dto.LeaderboardEntryResponse;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.repository.TournamentRepository;
import com.example.balloon.repository.redis.TournamentRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Фоновый наблюдатель за турниром — аналог горутины service.TournamentWatcher из Go.
 * Раз в 10 секунд проверяет, не закончился ли активный турнир; если закончился —
 * определяет победителя по лидерборду, переносит турнир в архив (Postgres)
 * и очищает ключи в Redis.
 */
@Component
@Slf4j
public class TournamentWatcher {

    private final TournamentRedisRepository tournamentRedis;
    private final TournamentRepository tournamentRepository;

    public TournamentWatcher(TournamentRedisRepository tournamentRedis,
                             TournamentRepository tournamentRepository) {
        this.tournamentRedis = tournamentRedis;
        this.tournamentRepository = tournamentRepository;
    }

    @Scheduled(fixedDelay = 10_000, initialDelay = 10_000)
    public void watch() {
        Optional<ActiveTournamentResponse> maybeActive;
        try {
            maybeActive = tournamentRedis.getActiveTournament();
        } catch (Exception e) {
            log.warn("Наблюдатель турниров: Redis недоступен: {}", e.getMessage());
            return;
        }

        if (maybeActive.isEmpty()) {
            return;
        }

        ActiveTournamentResponse active = maybeActive.get();

        if (active.getEndsAt() == null || Instant.now().isBefore(active.getEndsAt())) {
            log.debug("not ended yet");
            return;
        }

        String winner = null;
        try {
            List<LeaderboardEntryResponse> top = tournamentRedis.getTournamentTop(active.getId(), 1);
            if (!top.isEmpty()) {
                winner = top.getFirst().getUserName();
            }
        } catch (Exception e) {
            log.warn("Не удалось определить победителя турнира {}: {}", active.getId(), e.getMessage());
        }

        TournamentEntity archived = new TournamentEntity();
        archived.setName(active.getName());
        archived.setStartedAt(active.getStartedAt());
        archived.setEndedAt(active.getEndsAt());
        archived.setWinner(winner);

        // без внешней транзакции save коммитит сразу: ключи Redis удаляются только после успешной записи в БД
        try {
            tournamentRepository.save(archived);
        } catch (Exception e) {
            log.error("db error: {}", e.getMessage());
            return;
        }

        tournamentRedis.deleteActiveTournament();
        tournamentRedis.deleteLeaderboard(active.getId());

        log.info("Турнир '{}' завершён, победитель: {}", active.getName(), winner == null ? "нет" : winner);
    }
}

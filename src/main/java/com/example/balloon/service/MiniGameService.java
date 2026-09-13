package com.example.balloon.service;

import com.example.balloon.exception.BadRequestException;
import com.example.balloon.exception.ForbiddenException;
import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.leaderboard.MiniGameLeaderboardProjection;
import com.example.balloon.model.dto.game.*;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.MiniGameSessionEntity;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.repository.GameHistoryRepository;
import com.example.balloon.repository.MiniGameSessionRepository;
import com.example.balloon.repository.RewardRepository;
import com.example.balloon.repository.redis.GameSessionRedisRepository;
import com.example.balloon.repository.redis.TournamentRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class MiniGameService {

    private final MiniGameSessionRepository sessionRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final RewardRepository rewardRepository;
    private final GameSessionRedisRepository sessionRedis;
    private final TournamentRedisRepository tournamentRedis;
    private final GameHashService gameHashService;
    private final GameConfigService gameConfigService;

    public MiniGameService(MiniGameSessionRepository sessionRepository,
                           GameHistoryRepository gameHistoryRepository,
                           RewardRepository rewardRepository,
                           GameSessionRedisRepository sessionRedis,
                           TournamentRedisRepository tournamentRedis,
                           GameHashService gameHashService,
                           GameConfigService gameConfigService) {
        this.sessionRepository = sessionRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.rewardRepository = rewardRepository;
        this.sessionRedis = sessionRedis;
        this.tournamentRedis = tournamentRedis;
        this.gameHashService = gameHashService;
        this.gameConfigService = gameConfigService;
    }

    private static String nowRfc3339() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    private static String startOfTodayUtc() {
        return Instant.now().truncatedTo(ChronoUnit.DAYS).toString();
    }

    @Transactional
    public StartMiniGameResponse start(String userName) {
        GameConfig cfg = gameConfigService.get();

        if (!cfg.isAllowReplay() || cfg.getMaxGamesPerDay() > 0) {
            long playedToday = gameHistoryRepository
                    .countByUserNameAndPlayedAtGreaterThanEqual(userName, startOfTodayUtc());

            if (!cfg.isAllowReplay() && playedToday > 0) {
                throw new ForbiddenException("replay is disabled, you already played today");
            }
            if (cfg.getMaxGamesPerDay() > 0 && playedToday >= cfg.getMaxGamesPerDay()) {
                throw new ForbiddenException("daily games limit reached");
            }
        }

        String serverSeed = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();

        MiniGameSessionEntity session = new MiniGameSessionEntity();
        session.setUserName(userName);
        session.setScore(0);
        session.setStartedAt(nowRfc3339());
        session.setFinished(false);
        session.setServerSeed(serverSeed);

        String sessionId = sessionRepository.save(session).getId();

        sessionRedis.save(new ActiveGameSessionDTO(
                sessionId,
                userName,
                Instant.now().getEpochSecond(),
                serverSeed,
                secret
        ));

        return new StartMiniGameResponse(sessionId, cfg.getGameDuration(), secret);
    }

    @Transactional
    public FinishMiniGameResponse finish(FinishMiniGameRequest req) {
        GameConfig cfg = gameConfigService.get();

        ActiveGameSessionDTO session = sessionRedis.find(req.getSessionId())
                .orElseThrow(() -> new NotFoundException("session not found"));

        int score = req.getScore() == null ? 0 : req.getScore();

        String expectedHash = gameHashService.buildGameHash(req.getSessionId(), score, session.getSecret());
        if (!gameHashService.matches(expectedHash, req.getHash())) {
            throw new ForbiddenException("invalid hash");
        }

        long duration = Instant.now().getEpochSecond() - session.getStartedAt();
        if (duration < cfg.getMinPlayTime()) {
            throw new BadRequestException("suspicious result");
        }

        int calculatedScore = score;
        if (cfg.getScoreMultiplier() > 0) {
            calculatedScore = (int) (calculatedScore * cfg.getScoreMultiplier());
        }
        if (cfg.getMaxScore() > 0 && calculatedScore > cfg.getMaxScore()) {
            calculatedScore = cfg.getMaxScore();
        }
        int finalScore = calculatedScore;

        String userName = session.getUserName();

        GameHistoryEntity history = new GameHistoryEntity();
        history.setUserName(userName);
        history.setScore(finalScore);
        history.setPlayedAt(nowRfc3339());
        history.setIsSuccess(true);
        gameHistoryRepository.save(history);

        sessionRepository.findById(req.getSessionId()).ifPresent(entity -> {
            entity.setScore(finalScore);
            entity.setFinished(true);
            sessionRepository.save(entity);
        });

        addScoreToActiveTournament(userName, finalScore);

        sessionRedis.delete(req.getSessionId());

        boolean rewardCreated = false;
        if (cfg.isEnableRewards() && ThreadLocalRandom.current().nextDouble() < cfg.getRewardChance()) {
            RewardEntity reward = new RewardEntity();
            reward.setName(cfg.getRewardName());
            reward.setClaimed(false);
            reward.setUserName(userName);
            rewardRepository.save(reward);
            rewardCreated = true;
        }

        boolean bonusGranted = false;
        if (cfg.isEnableBonuses() && cfg.getBonusEvery() > 0) {
            long rewardsCount = rewardRepository.countByUserName(userName);
            boolean bonusMilestone = rewardsCount > 0 && rewardsCount % cfg.getBonusEvery() == 0;

            if (bonusMilestone && ThreadLocalRandom.current().nextDouble() < cfg.getBonusRewardChance()) {
                GameHistoryEntity bonusHistory = new GameHistoryEntity();
                bonusHistory.setUserName(userName);
                bonusHistory.setScore(cfg.getBonusScore());
                bonusHistory.setPlayedAt(nowRfc3339());
                bonusHistory.setIsSuccess(true);
                gameHistoryRepository.save(bonusHistory);

                addScoreToActiveTournament(userName, cfg.getBonusScore());
                bonusGranted = true;
            }
        }

        return new FinishMiniGameResponse(
                "game finished",
                finalScore,
                rewardCreated,
                bonusGranted,
                req.getSessionId()
        );
    }

    private void addScoreToActiveTournament(String userName, int score) {
        try {
            tournamentRedis.getActiveTournament().ifPresent(tournament ->
                    tournamentRedis.addTournamentScore(tournament.getId(), userName, score));
        } catch (Exception e) {
            log.warn("Не удалось начислить очки в лидерборд турнира: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<MiniGameLeaderboardProjection> getLeaderboard() {
        return gameHistoryRepository.findLeaderboard();
    }
}

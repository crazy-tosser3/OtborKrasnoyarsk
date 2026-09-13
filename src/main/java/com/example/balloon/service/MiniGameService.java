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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MiniGameService {

    /** Длительность раунда, секунды. */
    private static final int GAME_DURATION_SECONDS = 30;
    /** Минимальная правдоподобная длительность раунда, секунды. */
    private static final long MIN_PLAY_SECONDS = 5;
    /** Каждая N-я награда игрока даёт бонусные очки. */
    private static final long BONUS_EVERY = 4;
    private static final int BONUS_SCORE = 500;
    private static final String REWARD_NAME = "Супер награда";

    private final MiniGameSessionRepository sessionRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final RewardRepository rewardRepository;
    private final GameSessionRedisRepository sessionRedis;
    private final TournamentRedisRepository tournamentRedis;
    private final GameHashService gameHashService;

    public MiniGameService(MiniGameSessionRepository sessionRepository,
                           GameHistoryRepository gameHistoryRepository,
                           RewardRepository rewardRepository,
                           GameSessionRedisRepository sessionRedis,
                           TournamentRedisRepository tournamentRedis,
                           GameHashService gameHashService) {
        this.sessionRepository = sessionRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.rewardRepository = rewardRepository;
        this.sessionRedis = sessionRedis;
        this.tournamentRedis = tournamentRedis;
        this.gameHashService = gameHashService;
    }

    private static String nowRfc3339() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }

    /**
     * Старт раунда. Сессия с секретом кладётся в Redis (TTL 30 минут),
     * параллельно пишется строка в mini_game_sessions — по ней строится лидерборд мини-игры.
     */
    @Transactional
    public StartMiniGameResponse start(String userName) {
        String serverSeed = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();

        // Идентификатор выдаёт JPA, тот же id используется ключом сессии в Redis
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

        return new StartMiniGameResponse(sessionId, GAME_DURATION_SECONDS, secret);
    }

    /**
     * Завершение раунда: проверка хеша и длительности, запись истории,
     * начисление очков в лидерборд турнира, выдача награды и бонуса за каждую 4-ю награду.
     */
    @Transactional
    public FinishMiniGameResponse finish(FinishMiniGameRequest req) {
        ActiveGameSessionDTO session = sessionRedis.find(req.getSessionId())
                .orElseThrow(() -> new NotFoundException("session not found"));

        int score = req.getScore() == null ? 0 : req.getScore();

        String expectedHash = gameHashService.buildGameHash(req.getSessionId(), score, session.getSecret());
        if (!gameHashService.matches(expectedHash, req.getHash())) {
            throw new ForbiddenException("invalid hash");
        }

        long duration = Instant.now().getEpochSecond() - session.getStartedAt();
        if (duration < MIN_PLAY_SECONDS) {
            throw new BadRequestException("suspicious result");
        }

        String userName = session.getUserName();

        GameHistoryEntity history = new GameHistoryEntity();
        history.setUserName(userName);
        history.setScore(score);
        history.setPlayedAt(nowRfc3339());
        history.setIsSuccess(true);
        gameHistoryRepository.save(history);

        sessionRepository.findById(req.getSessionId()).ifPresent(entity -> {
            entity.setScore(score);
            entity.setFinished(true);
            sessionRepository.save(entity);
        });

        addScoreToActiveTournament(userName, score);

        sessionRedis.delete(req.getSessionId());

        RewardEntity reward = new RewardEntity();
        reward.setName(REWARD_NAME);
        reward.setClaimed(false);
        reward.setUserName(userName);
        rewardRepository.save(reward);

        long rewardsCount = rewardRepository.countByUserName(userName);
        boolean bonusGranted = false;

        if (rewardsCount > 0 && rewardsCount % BONUS_EVERY == 0) {
            GameHistoryEntity bonusHistory = new GameHistoryEntity();
            bonusHistory.setUserName(userName);
            bonusHistory.setScore(BONUS_SCORE);
            bonusHistory.setPlayedAt(nowRfc3339());
            bonusHistory.setIsSuccess(true);
            gameHistoryRepository.save(bonusHistory);

            addScoreToActiveTournament(userName, BONUS_SCORE);
            bonusGranted = true;
        }

        return new FinishMiniGameResponse(
                "game finished",
                score,
                true,
                bonusGranted,
                req.getSessionId()
        );
    }

    /** Очки уходят в ZSET активного турнира; отсутствие турнира не должно ломать игру. */
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
        return sessionRepository.findLeaderboard();
    }
}

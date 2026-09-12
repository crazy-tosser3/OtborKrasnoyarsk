package com.example.balloon.service;

import com.example.balloon.exception.BadRequestException;
import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.game.LeaderboardEntryResponse;
import com.example.balloon.model.dto.minigame.FinishMiniGameRequest;
import com.example.balloon.model.dto.minigame.StartMiniGameResponse;
import com.example.balloon.model.entity.GameHistoryEntity;
import com.example.balloon.model.entity.MiniGameSessionEntity;
import com.example.balloon.repository.GameHistoryRepository;
import com.example.balloon.repository.MiniGameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MiniGameService {

    private final MiniGameSessionRepository sessionRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public MiniGameService(MiniGameSessionRepository sessionRepository,
                           GameHistoryRepository gameHistoryRepository) {
        this.sessionRepository = sessionRepository;
        this.gameHistoryRepository = gameHistoryRepository;
    }

    @Transactional
    public StartMiniGameResponse start(String userName) {
        MiniGameSessionEntity session = new MiniGameSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setUserName(userName);
        session.setScore(0);
        session.setStartedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        session.setFinished(false);

        sessionRepository.save(session);

        return new StartMiniGameResponse(session.getId(), 30);
    }

    @Transactional
    public Map<String, Object> finish(FinishMiniGameRequest req) {
        MiniGameSessionEntity session = sessionRepository.findById(req.getSessionId())
                .orElseThrow(() -> new NotFoundException("session not found"));

        if (session.isFinished()) {
            throw new BadRequestException("session already finished");
        }

        session.setScore(req.getScore());
        session.setFinished(true);
        sessionRepository.save(session);

        GameHistoryEntity history = new GameHistoryEntity();
        history.setUserName(session.getUserName());
        history.setScore(req.getScore());
        history.setPlayedAt(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        gameHistoryRepository.save(history);

        return Map.of(
                "message", "game finished",
                "score", req.getScore()
        );
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard() {
        return sessionRepository.getLeaderboard().stream()
                .map(p -> new LeaderboardEntryResponse(
                        p.getUserName(),
                        p.getScore() != null ? p.getScore().intValue() : 0))
                .toList();
    }
}
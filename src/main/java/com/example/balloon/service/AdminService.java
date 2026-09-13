package com.example.balloon.service;

import com.example.balloon.exception.BadRequestException;
import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.*;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.model.entity.UserEntity;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.repository.GameHistoryRepository;
import com.example.balloon.repository.RewardRepository;
import com.example.balloon.repository.TournamentRepository;
import com.example.balloon.repository.UserRepository;
import com.example.balloon.repository.redis.TournamentRedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;
    private final RewardRepository rewardRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final TournamentRedisRepository tournamentRedis;
    private final EntityMapper mapper;

    public AdminService(UserRepository userRepository,
                        TournamentRepository tournamentRepository,
                        RewardRepository rewardRepository,
                        GameHistoryRepository gameHistoryRepository,
                        TournamentRedisRepository tournamentRedis,
                        EntityMapper mapper) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.rewardRepository = rewardRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.tournamentRedis = tournamentRedis;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return mapper.toUserResponseList(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String userName) {
        return userRepository.findByUserName(userName)
                .map(mapper::toUserResponse)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    @Transactional
    public UserResponse changeRole(ChangeRoleRequest req) {
        UserEntity user = userRepository.findByUserName(req.getUserName())
                .orElseThrow(() -> new NotFoundException("user not found"));
        user.setUserRole(req.getRole());
        return mapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public String deleteUser(String userName) {
        UserEntity user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new NotFoundException("user not found"));
        userRepository.delete(user);
        return "user deleted";
    }

    /** Активный турнир (из Redis) — тот же список, что отдаёт публичный /api/tournament. */
    public List<ActiveTournamentResponse> getTournaments() {
        return tournamentRedis.getActiveTournament()
                .map(List::of)
                .orElseGet(List::of);
    }

    /**
     * Создание турнира: активный турнир пишется в Redis под ключ tournament:active.
     * Идентификатором, как и в Go-версии, служит имя турнира.
     */
    public ActiveTournamentResponse createTournament(CreateTournamentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("tournament name is required");
        }

        Instant startedAt = request.getStartedAt() != null
                ? request.getStartedAt()
                : Instant.now();
        Instant endsAt = request.getEndsAt() != null
                ? request.getEndsAt()
                : startedAt.plus(1, ChronoUnit.DAYS);

        if (!endsAt.isAfter(startedAt)) {
            throw new BadRequestException("tournament end time must be after start time");
        }
        if (!endsAt.isAfter(Instant.now())) {
            throw new BadRequestException("tournament end time must be in the future");
        }

        ActiveTournamentResponse tournament = new ActiveTournamentResponse(
                request.getName(),
                request.getName(),
                startedAt,
                endsAt
        );

        tournamentRedis.setActiveTournament(tournament);
        return tournament;
    }

    /**
     * Удаление турнира: если id совпадает с активным — чистим Redis
     * (сам турнир и его лидерборд), иначе удаляем запись из архива.
     */
    @Transactional
    public String deleteTournament(String id) {
        Optional<ActiveTournamentResponse> active = tournamentRedis.getActiveTournament();

        if (active.isPresent() && active.get().getId().equals(id)) {
            tournamentRedis.deleteActiveTournament();
            tournamentRedis.deleteLeaderboard(id);
            return "tournament deleted";
        }

        tournamentRepository.delete(tournamentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("tournament not found")));
        return "tournament deleted";
    }

    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        RewardEntity newReward = new RewardEntity();
        newReward.setName(request.getName());
        newReward.setClaimed(request.getClaimed() != null && request.getClaimed());
        newReward.setUserName(request.getUserName());
        return mapper.toRewardResponse(rewardRepository.save(newReward));
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getRewards() {
        return mapper.toRewardResponseList(rewardRepository.findAll());
    }

    /** История игр, новые сверху — как ORDER BY played_at DESC в Go-версии. */
    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getGames() {
        return mapper.toGameHistoryResponseList(gameHistoryRepository.findAllByOrderByPlayedAtDesc());
    }
}

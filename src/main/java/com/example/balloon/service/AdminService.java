package com.example.balloon.service;

import com.example.balloon.exception.BadRequestException;
import com.example.balloon.exception.NotFoundException;
import com.example.balloon.model.dto.game.GameHistoryResponse;
import com.example.balloon.model.dto.reward.RewardRequest;
import com.example.balloon.model.dto.reward.RewardResponse;
import com.example.balloon.model.dto.tournament.ActiveTournamentResponse;
import com.example.balloon.model.dto.tournament.CreateTournamentRequest;
import com.example.balloon.model.dto.user.ChangeRoleRequest;
import com.example.balloon.model.dto.user.UserResponse;
import com.example.balloon.model.entity.RewardEntity;
import com.example.balloon.model.entity.TournamentEntity;
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

    public List<ActiveTournamentResponse> getTournaments() {
        return tournamentRedis.getActiveTournament()
                .map(List::of)
                .orElseGet(List::of);
    }

    public ActiveTournamentResponse createTournament(CreateTournamentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("tournament name is required");
        }

        Instant startedAt = (request.getStartedAt() != null
                ? request.getStartedAt()
                : Instant.now()).truncatedTo(ChronoUnit.MICROS);
        Instant endsAt = (request.getEndsAt() != null
                ? request.getEndsAt()
                : startedAt.plus(1, ChronoUnit.DAYS)).truncatedTo(ChronoUnit.MICROS);

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

        TournamentEntity entity = new TournamentEntity();
        entity.setName(request.getName());
        entity.setStartedAt(startedAt);
        entity.setEndedAt(endsAt);

        tournamentRedis.setActiveTournament(tournament);
        tournamentRepository.save(entity);
        return tournament;
    }

    @Transactional
    public String deleteTournament(String id) {
        Optional<ActiveTournamentResponse> active = tournamentRedis.getActiveTournament();

        if (active.isPresent() && active.get().getId().equals(id)) {
            tournamentRepository.findFirstByNameAndStartedAt(active.get().getName(), active.get().getStartedAt())
                    .ifPresent(tournamentRepository::delete);
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

    @Transactional(readOnly = true)
    public List<GameHistoryResponse> getGames() {
        return mapper.toGameHistoryResponseList(gameHistoryRepository.findAllByOrderByPlayedAtDesc());
    }
}

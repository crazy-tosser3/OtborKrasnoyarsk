//package com.example.demo.service;
//
//
//import com.example.demo.repository.*;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Objects;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final GameHistoryRepository gameHistoryRepository;
//    private final LeaderboardEntryRepository leaderboardEntryRepository;
//    private final TournamentRepository tournamentRepository;
//    private final RewardRepository rewardRepository;
//    private final PasswordService passwordService;
//
//    public UserService(UserRepository userRepository,
//                       GameHistoryRepository gameHistoryRepository,
//                       LeaderboardEntryRepository leaderboardEntryRepository,
//                       TournamentRepository tournamentRepository,
//                       RewardRepository rewardRepository,
//                       PasswordService passwordService) {
//        this.userRepository = userRepository;
//        this.gameHistoryRepository = gameHistoryRepository;
//        this.leaderboardEntryRepository = leaderboardEntryRepository;
//        this.tournamentRepository = tournamentRepository;
//        this.rewardRepository = rewardRepository;
//        this.passwordService = passwordService;
//    }
//
//    // ---------- login ----------
//    public void login(UserLogin req) {
//        UserEntity user = userRepository.findById(req.getUserName())
//                .orElseThrow(() -> new UnauthorizedException("user not found"));
//
//        byte[] salt = passwordService.decodeSalt(user.getSalt());
//        if (!passwordService.hashPassword(req.getUserPassword(), salt).equals(user.getPasswordHash())) {
//            throw new UnauthorizedException("wrong password");
//        }
//    }
//
//    // ---------- register ----------
//    public void register(UserRegister req) {
//        if (userRepository.existsById(req.getUserName())) {
//            throw new ConflictException("user already exists");
//        }
//        byte[] salt = passwordService.generateSalt();
//        UserEntity user = new UserEntity(
//                req.getUserName(),
//                req.getUserEmail(),
//                passwordService.hashPassword(req.getUserPassword(), salt),
//                passwordService.encodeSalt(salt)
//        );
//        userRepository.save(user);
//    }
//
//    // ---------- update ----------
//    public void update(UserUpdate req) {
//        UserEntity user = userRepository.findById(req.getUserName())
//                .orElseThrow(() -> new NotFoundException("user not found"));
//
//        byte[] salt = passwordService.decodeSalt(user.getSalt());
//        if (!passwordService.hashPassword(req.getUserPassword(), salt).equals(user.getPasswordHash())) {
//            throw new UnauthorizedException("wrong password");
//        }
//
//        if (!Objects.equals(req.getNewUserName(), req.getUserName())
//                && userRepository.existsById(req.getNewUserName())) {
//            throw new BadRequestException("new username already exists");
//        }
//
//        userRepository.delete(user);
//
//        byte[] newSalt = passwordService.generateSalt();
//        user.setUserName(req.getNewUserName());
//        user.setPasswordHash(passwordService.hashPassword(req.getNewUserPassword(), newSalt));
//        user.setSalt(passwordService.encodeSalt(newSalt));
//
//        userRepository.save(user);
//    }
//
//    // ---------- delete ----------
//    public void delete(UserDelete req) {
//        UserEntity user = userRepository.findById(req.getUserName())
//                .orElseThrow(() -> new NotFoundException("user not found"));
//
//        byte[] salt = passwordService.decodeSalt(user.getSalt());
//        if (!passwordService.hashPassword(req.getUserPassword(), salt).equals(user.getPasswordHash())) {
//            throw new UnauthorizedException("wrong password");
//        }
//        userRepository.delete(user);
//    }
//
//    // ---------- profile ----------
//    public UserProfile profile(String username) {
//        UserEntity user = userRepository.findById(username)
//                .orElseThrow(() -> new NotFoundException("user not found"));
//        return new UserProfile(user.getUserName(), user.getUserEmail());
//    }
//
//    // ---------- games ----------
//    public List<GameHistoryEntity> getGlobalHistory() {
//        return gameHistoryRepository.findAll();
//    }
//
//    // ---------- leaderboard ----------
//    public List<LeaderboardEntryEntity> getLeaderboard() {
//        return leaderboardRepository.findAll();
//    }
//
//    // ---------- tournament ----------
//    public TournamentEntity getCurrentTournament() {
//        return tournamentRepository.findCurrent()
//                .orElseThrow(() -> new NotFoundException("tournament not found"));
//    }
//
//    // ---------- rewards ----------
//    public List<RewardEntity> getRewards(String username) {
//        return rewardRepository.findByUserName(username);
//    }
//
//    public RewardEntity claimReward(String rewardId) {
//        RewardEntity reward = rewardRepository.findById(rewardId)
//                .orElseThrow(() -> new NotFoundException("reward not found"));
//        reward.setClaimed(true);
//        return rewardRepository.save(reward);
//    }
//}
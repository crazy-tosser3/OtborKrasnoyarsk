package com.example.balloon.repository;

import com.example.balloon.model.entity.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<TournamentEntity, String> {

    Optional<TournamentEntity> findFirstByNameAndStartedAt(String name, Instant startedAt);

    List<TournamentEntity> findAllByOrderByEndedAtDesc();
}

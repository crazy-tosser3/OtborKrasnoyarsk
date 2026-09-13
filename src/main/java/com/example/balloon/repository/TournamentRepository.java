package com.example.balloon.repository;

import com.example.balloon.model.entity.TournamentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<TournamentEntity, String> {

    /** Архив завершённых турниров, новые сверху. */
    List<TournamentEntity> findAllByOrderByEndedAtDesc();
}

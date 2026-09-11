package com.example.balloon.service;

import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;

    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    public TournamentEntity create(TournamentEntity tournament) {
        return tournamentRepository.save(tournament);
    }

    @Transactional(readOnly = true)
    public List<TournamentEntity> getAll() {
        return tournamentRepository.findAll();
    }

    @Transactional
    public void delete(String id) {
        tournamentRepository.deleteById(id);
    }
}
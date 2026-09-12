package com.example.balloon.service;

import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.repository.TournamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final EntityMapper mapper;

    public TournamentService(TournamentRepository tournamentRepository, EntityMapper mapper) {
        this.tournamentRepository = tournamentRepository;
        this.mapper = mapper;
    }

    @Transactional
    public TournamentResponse create(TournamentEntity tournament) {
        TournamentEntity saved = tournamentRepository.save(tournament);
        return mapper.toTournamentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> getAll() {
        return mapper.toTournamentResponseList(tournamentRepository.findAll());
    }

    @Transactional
    public void delete(String id) {
        tournamentRepository.deleteById(id);
    }
}
package com.example.balloon.controller;

import com.example.balloon.model.dto.tournament.TournamentRequest;
import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.model.mapper.EntityMapper;
import com.example.balloon.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tournaments")
@RequiredArgsConstructor
public class AdminTournamentController {

    private final TournamentService tournamentService;
    private final EntityMapper mapper;

    @PostMapping
    public ResponseEntity<TournamentResponse> createTournament(@RequestBody TournamentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tournamentService.create(mapper.toTournamentEntity(req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable String id) {
        tournamentService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
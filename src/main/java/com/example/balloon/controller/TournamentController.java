package com.example.balloon.controller;

import com.example.balloon.model.dto.tournament.TournamentResponse;
import com.example.balloon.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAll() {
        return ResponseEntity.ok(tournamentService.getAll());
    }
}
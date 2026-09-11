package com.example.balloon.controller;

import com.example.balloon.model.entity.TournamentEntity;
import com.example.balloon.service.TournamentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentEntity create(@RequestBody TournamentEntity tournament) {
        return tournamentService.create(tournament);
    }

    @GetMapping
    public List<TournamentEntity> getAll() {
        return tournamentService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        tournamentService.delete(id);
    }
}
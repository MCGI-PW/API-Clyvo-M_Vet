package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Raca;
import com.clyvo.veterinary.repositories.RacaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/racas")
public class RacaController {
    private final RacaRepository repo;
    public RacaController(RacaRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Raca> listAll() { return repo.findAll(); }
}

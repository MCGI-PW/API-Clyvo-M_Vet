package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Raca;
import com.clyvo.veterinary.services.RacaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/racas")
public class RacaController {

    private final RacaService racaService;

    public RacaController(RacaService racaService) {
        this.racaService = racaService;
    }

    @GetMapping
    public List<Raca> listAll() {
        return racaService.listAll();
    }
}

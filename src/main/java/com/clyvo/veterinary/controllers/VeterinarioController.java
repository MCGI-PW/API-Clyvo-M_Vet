package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Veterinario;
import com.clyvo.veterinary.repositories.VeterinarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {
    private final VeterinarioRepository repo;
    public VeterinarioController(VeterinarioRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Veterinario> listAll() { return repo.findAll(); }
}

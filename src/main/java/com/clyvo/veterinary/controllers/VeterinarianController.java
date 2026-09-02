package com.clyvo.veterinary.controllers;
import com.clyvo.veterinary.models.Veterinarian;
import com.clyvo.veterinary.repositories.VeterinarianRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarians")
public class VeterinarianController {
    private final VeterinarianRepository repo;
    public VeterinarianController(VeterinarianRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Veterinarian> listAll() { return repo.findAll(); }
}

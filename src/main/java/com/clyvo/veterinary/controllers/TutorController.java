package com.clyvo.veterinary.controllers;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {
    private final TutorRepository repo;
    public TutorController(TutorRepository repo) { this.repo = repo; }
    
    @GetMapping
    public List<Tutor> listAll() { return repo.findAll(); }
}

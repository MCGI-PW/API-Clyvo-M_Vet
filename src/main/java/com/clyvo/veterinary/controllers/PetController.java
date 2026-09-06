package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.PetRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetController(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta).orElseThrow();
        
        pet.setIdPet(null);
        pet.setTutor(tutor);
        return ResponseEntity.ok(petRepository.save(pet));
    }

    @GetMapping
    public ResponseEntity<List<Pet>> myPets() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta).orElseThrow();
        
        return ResponseEntity.ok(petRepository.findByTutorIdTutor(tutor.getIdTutor()));
    }
}

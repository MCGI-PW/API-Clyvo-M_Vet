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
@PreAuthorize("hasRole('TUTOR')")
public class PetController {
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetController(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(userIdStr);
        Tutor tutor = tutorRepository.findByUserId(userId).orElseThrow();
        
        pet.setTutor(tutor);
        return ResponseEntity.ok(petRepository.save(pet));
    }

    @GetMapping
    public ResponseEntity<List<Pet>> myPets() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = UUID.fromString(userIdStr);
        Tutor tutor = tutorRepository.findByUserId(userId).orElseThrow();
        
        return ResponseEntity.ok(petRepository.findByTutorId(tutor.getId()));
    }
}

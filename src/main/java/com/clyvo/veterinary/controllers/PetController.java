package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.services.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    private UUID getLoggedAccountId() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(idContaStr);
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        return ResponseEntity.ok(petService.createPet(pet, getLoggedAccountId()));
    }

    @GetMapping
    public ResponseEntity<List<Pet>> myPets() {
        return ResponseEntity.ok(petService.listPetsByConta(getLoggedAccountId()));
    }
}

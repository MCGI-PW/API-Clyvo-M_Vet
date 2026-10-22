package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.PetRequest;
import com.clyvo.veterinary.models.Pet;
import jakarta.validation.Valid;
import com.clyvo.veterinary.services.PetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    public ResponseEntity<Pet> createPet(@Valid @RequestBody PetRequest request) {
        return ResponseEntity.ok(petService.createPet(request.toEntity(), getLoggedAccountId()));
    }

    @GetMapping
    public ResponseEntity<List<Pet>> myPets() {
        return ResponseEntity.ok(petService.listPetsByConta(getLoggedAccountId()));
    }
}

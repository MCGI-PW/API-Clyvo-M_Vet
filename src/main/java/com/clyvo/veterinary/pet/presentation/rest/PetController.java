package com.clyvo.veterinary.pet.presentation.rest;

import com.clyvo.veterinary.pet.application.dto.CreatePetRequest;
import com.clyvo.veterinary.pet.application.dto.PetResponse;
import com.clyvo.veterinary.pet.application.dto.UpdatePetRequest;
import com.clyvo.veterinary.pet.application.port.in.PetUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Endpoints for managing pets")
public class PetController {

    private final PetUseCase petUseCase;

    public PetController(PetUseCase petUseCase) {
        this.petUseCase = petUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public PetResponse createPet(@RequestBody @Valid CreatePetRequest request) {
        return petUseCase.createPet(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public PetResponse getPet(@PathVariable UUID id) {
        return petUseCase.getPet(id);
    }

    @GetMapping("/tutor/{tutorId}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public List<PetResponse> listPetsByTutor(@PathVariable UUID tutorId) {
        return petUseCase.listPetsByTutor(tutorId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public PetResponse updatePet(@PathVariable UUID id, @RequestBody @Valid UpdatePetRequest request) {
        return petUseCase.updatePet(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public void deletePet(@PathVariable UUID id) {
        petUseCase.deletePet(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PetResponse> listAllPets() {
        return petUseCase.listAll();
    }
}

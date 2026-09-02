package com.clyvo.veterinary.vaccine.presentation.rest;

import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.application.port.in.VaccineUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vaccines")
@Tag(name = "Vaccines", description = "Endpoints for managing pet vaccines")
public class VaccineController {

    private final VaccineUseCase vaccineUseCase;

    public VaccineController(VaccineUseCase vaccineUseCase) {
        this.vaccineUseCase = vaccineUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Register vaccine", description = "Registers a new vaccine for a pet")
    public ResponseEntity<VaccineResponse> registerVaccine(
            @Valid @RequestBody CreateVaccineRequest request) {

        VaccineResponse response = vaccineUseCase.registerVaccine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @Operation(summary = "Get vaccine by ID")
    public ResponseEntity<VaccineResponse> getVaccine(@PathVariable UUID id) {
        return ResponseEntity.ok(vaccineUseCase.getVaccine(id));
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @Operation(summary = "List vaccines by pet")
    public ResponseEntity<List<VaccineResponse>> listByPet(@PathVariable UUID petId) {
        return ResponseEntity.ok(vaccineUseCase.listByPet(petId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @Operation(summary = "Delete vaccine")
    public ResponseEntity<Void> deleteVaccine(@PathVariable UUID id) {
        vaccineUseCase.deleteVaccine(id);
        return ResponseEntity.noContent().build();
    }
}

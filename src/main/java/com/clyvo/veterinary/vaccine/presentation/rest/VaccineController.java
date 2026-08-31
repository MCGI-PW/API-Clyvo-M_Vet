package com.clyvo.veterinary.vaccine.presentation.rest;

import com.clyvo.veterinary.shared.application.security.CurrentUser;
import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.application.port.in.VaccineUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vaccines")
@Tag(name = "Vacinas", description = "Gerenciamento do histórico de vacinação dos pets")
public class VaccineController {

    private final VaccineUseCase vaccineUseCase;

    public VaccineController(VaccineUseCase vaccineUseCase) {
        this.vaccineUseCase = vaccineUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIAN')")
    @Operation(summary = "Registrar vacina", description = "Registra uma vacina aplicada a um pet pelo veterinário logado")
    public ResponseEntity<VaccineResponse> registerVaccine(
            @CurrentUser UserDetails currentUser,
            @Valid @RequestBody CreateVaccineRequest request) {

        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        VaccineResponse response = vaccineUseCase.registerVaccine(veterinarianId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'TUTOR', 'ADMIN')")
    @Operation(summary = "Buscar vacina por ID")
    public ResponseEntity<VaccineResponse> getVaccine(@PathVariable UUID id) {
        return ResponseEntity.ok(vaccineUseCase.getVaccine(id));
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'TUTOR')")
    @Operation(summary = "Histórico de vacinas por pet")
    public ResponseEntity<List<VaccineResponse>> listByPet(@PathVariable UUID petId) {
        return ResponseEntity.ok(vaccineUseCase.listByPet(petId));
    }

    @GetMapping("/vet/me")
    @PreAuthorize("hasRole('VETERINARIAN')")
    @Operation(summary = "Vacinas registradas pelo vet logado")
    public ResponseEntity<List<VaccineResponse>> myVaccines(@CurrentUser UserDetails currentUser) {
        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        return ResponseEntity.ok(vaccineUseCase.listByVeterinarian(veterinarianId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar registro de vacina (ADMIN)")
    public ResponseEntity<Void> deleteVaccine(@PathVariable UUID id) {
        vaccineUseCase.deleteVaccine(id);
        return ResponseEntity.noContent().build();
    }
}

package com.clyvo.veterinary.prescription.presentation.rest;

import com.clyvo.veterinary.prescription.application.dto.CreatePrescriptionRequest;
import com.clyvo.veterinary.prescription.application.dto.PrescriptionResponse;
import com.clyvo.veterinary.prescription.application.port.in.PrescriptionUseCase;
import com.clyvo.veterinary.shared.application.security.CurrentUser;
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
@RequestMapping("/api/prescriptions")
@Tag(name = "Prescriptions")
public class PrescriptionController {

    private final PrescriptionUseCase prescriptionUseCase;

    public PrescriptionController(PrescriptionUseCase prescriptionUseCase) {
        this.prescriptionUseCase = prescriptionUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<PrescriptionResponse> create(
            @CurrentUser UserDetails currentUser,
            @Valid @RequestBody CreatePrescriptionRequest request) {
        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionUseCase.createPrescription(veterinarianId, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VETERINARIAN','ADMIN')")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionUseCase.getPrescription(id));
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasAnyRole('VETERINARIAN','TUTOR')")
    public ResponseEntity<List<PrescriptionResponse>> listByPet(@PathVariable UUID petId) {
        return ResponseEntity.ok(prescriptionUseCase.listByPet(petId));
    }

    @GetMapping("/vet/me")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<List<PrescriptionResponse>> listMyPrescriptions(
            @CurrentUser UserDetails currentUser) {
        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        return ResponseEntity.ok(prescriptionUseCase.listByVeterinarian(veterinarianId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        prescriptionUseCase.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }
}

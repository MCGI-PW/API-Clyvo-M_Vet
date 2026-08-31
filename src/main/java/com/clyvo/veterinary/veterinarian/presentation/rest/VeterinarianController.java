package com.clyvo.veterinary.veterinarian.presentation.rest;

import com.clyvo.veterinary.veterinarian.application.dto.CreateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.UpdateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import com.clyvo.veterinary.veterinarian.application.port.in.VeterinarianUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/veterinarians")
@Tag(name = "Veterinarians", description = "Endpoints for veterinarian profile management")
public class VeterinarianController {

    private final VeterinarianUseCase veterinarianUseCase;

    public VeterinarianController(VeterinarianUseCase veterinarianUseCase) {
        this.veterinarianUseCase = veterinarianUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<VeterinarianResponse> createProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateVeterinarianRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(veterinarianUseCase.createProfile(userId, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<VeterinarianResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(veterinarianUseCase.getProfileByUserId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIAN', 'TUTOR')")
    public ResponseEntity<VeterinarianResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(veterinarianUseCase.getProfile(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<VeterinarianResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVeterinarianRequest request) {
        return ResponseEntity.ok(veterinarianUseCase.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID id) {
        veterinarianUseCase.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VeterinarianResponse>> listAll() {
        return ResponseEntity.ok(veterinarianUseCase.listAll());
    }
}

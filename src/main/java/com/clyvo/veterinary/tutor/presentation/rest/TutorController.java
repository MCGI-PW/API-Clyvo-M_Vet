package com.clyvo.veterinary.tutor.presentation.rest;

import com.clyvo.veterinary.tutor.application.dto.CreateTutorRequest;
import com.clyvo.veterinary.tutor.application.dto.TutorResponse;
import com.clyvo.veterinary.tutor.application.dto.UpdateTutorRequest;
import com.clyvo.veterinary.tutor.application.port.in.TutorUseCase;
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
@RequestMapping("/api/tutors")
@Tag(name = "Tutors", description = "Endpoints for tutor profile management")
public class TutorController {

    private final TutorUseCase tutorUseCase;

    public TutorController(TutorUseCase tutorUseCase) {
        this.tutorUseCase = tutorUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TutorResponse> createProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTutorRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(tutorUseCase.createProfile(userId, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TutorResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(tutorUseCase.getProfileByUserId(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<TutorResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(tutorUseCase.getProfile(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<TutorResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTutorRequest request) {
        return ResponseEntity.ok(tutorUseCase.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID id) {
        tutorUseCase.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TutorResponse>> listAll() {
        return ResponseEntity.ok(tutorUseCase.listAll());
    }
}

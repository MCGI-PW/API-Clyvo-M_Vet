package com.clyvo.veterinary.medicalrecord.presentation.rest;

import com.clyvo.veterinary.medicalrecord.application.dto.CreateMedicalRecordRequest;
import com.clyvo.veterinary.medicalrecord.application.dto.MedicalRecordResponse;
import com.clyvo.veterinary.medicalrecord.application.port.in.MedicalRecordUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordUseCase useCase;

    public MedicalRecordController(MedicalRecordUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('VETERINARIAN')")
    public MedicalRecordResponse createRecord(@RequestBody @Valid CreateMedicalRecordRequest request) {
        UUID vetId = UUID.randomUUID(); 
        return useCase.createRecord(vetId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public MedicalRecordResponse updateRecord(@PathVariable UUID id, @RequestParam(required = false) String observations, @RequestParam(required = false) String treatment) {
        return useCase.updateRecord(id, observations, treatment);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MedicalRecordResponse getRecord(@PathVariable UUID id) {
        return useCase.getRecord(id);
    }

    @GetMapping("/pet/{petId}")
    @PreAuthorize("isAuthenticated()")
    public List<MedicalRecordResponse> listByPet(@PathVariable UUID petId) {
        return useCase.listByPet(petId);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public MedicalRecordResponse getByAppointment(@PathVariable UUID appointmentId) {
        return useCase.getByAppointment(appointmentId);
    }
}

package com.clyvo.veterinary.medicalrecord.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMedicalRecordRequest(
        @NotNull UUID appointmentId,
        @NotNull UUID petId,
        @NotBlank String symptoms,
        @NotBlank String diagnosis,
        @NotBlank String treatment,
        String observations
) {}

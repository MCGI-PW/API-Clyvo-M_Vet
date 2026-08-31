package com.clyvo.veterinary.medicalrecord.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordResponse(
        UUID id,
        UUID appointmentId,
        UUID petId,
        String petName,
        UUID veterinarianId,
        String veterinarianName,
        String symptoms,
        String diagnosis,
        String treatment,
        String observations,
        LocalDateTime createdAt
) {}

package com.clyvo.veterinary.appointment.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID petId,
        String petName,
        UUID veterinarianId,
        String veterinarianName,
        String veterinarianCrm,
        UUID tutorId,
        String tutorName,
        LocalDateTime scheduledAt,
        String status,
        String notes,
        LocalDateTime createdAt
) {}

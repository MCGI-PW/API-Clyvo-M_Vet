package com.clyvo.veterinary.appointment.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleAppointmentRequest(
        @NotNull UUID petId,
        @NotNull UUID veterinarianId,
        @NotNull @Future LocalDateTime scheduledAt,
        @Size(max = 500) String notes
) {}

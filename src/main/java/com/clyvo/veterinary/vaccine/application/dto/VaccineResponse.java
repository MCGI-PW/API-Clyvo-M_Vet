package com.clyvo.veterinary.vaccine.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VaccineResponse(
    UUID id,
    UUID petId,
    String petName,
    String vaccineName,
    String manufacturer,
    String batchNumber,
    LocalDate appliedAt,
    LocalDate nextDoseAt,
    boolean nextDoseDue,
    String notes,
    LocalDateTime createdAt
) {}

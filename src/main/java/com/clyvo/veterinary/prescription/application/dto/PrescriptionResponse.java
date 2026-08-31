package com.clyvo.veterinary.prescription.application.dto;

import com.clyvo.veterinary.prescription.domain.model.PrescriptionItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
    UUID id,
    UUID petId,
    String petName,
    UUID veterinarianId,
    String veterinarianName,
    String veterinarianCrm,
    List<PrescriptionItem> medications,
    String generalInstructions,
    LocalDate validUntil,
    boolean valid,
    LocalDateTime createdAt
) {}

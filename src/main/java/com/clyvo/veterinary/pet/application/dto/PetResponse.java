package com.clyvo.veterinary.pet.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PetResponse(
        UUID id,
        UUID tutorId,
        String tutorName,
        String name,
        String species,
        String breed,
        LocalDate birthDate,
        Double weight,
        String color,
        String profilePictureUrl,
        boolean active,
        LocalDateTime createdAt
) {}

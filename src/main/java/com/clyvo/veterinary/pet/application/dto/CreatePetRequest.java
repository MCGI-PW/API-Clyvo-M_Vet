package com.clyvo.veterinary.pet.application.dto;

import com.clyvo.veterinary.pet.domain.model.Species;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePetRequest(
        @NotBlank String name,
        @NotNull Species species,
        String breed,
        LocalDate birthDate,
        Double weight,
        String color,
        String profilePictureUrl,
        @NotNull UUID tutorId
) {}

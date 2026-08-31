package com.clyvo.veterinary.pet.application.dto;

import com.clyvo.veterinary.pet.domain.model.Species;
import java.time.LocalDate;

public record UpdatePetRequest(
        String name,
        Species species,
        String breed,
        LocalDate birthDate,
        Double weight,
        String color,
        String profilePictureUrl
) {}

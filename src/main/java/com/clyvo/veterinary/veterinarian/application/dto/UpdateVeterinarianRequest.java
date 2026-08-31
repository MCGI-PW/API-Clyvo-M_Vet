package com.clyvo.veterinary.veterinarian.application.dto;

import com.clyvo.veterinary.veterinarian.domain.model.Specialty;
import jakarta.validation.constraints.Size;

public record UpdateVeterinarianRequest(
    Specialty specialty,

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    String bio,

    String phone,

    String profilePictureUrl
) {}

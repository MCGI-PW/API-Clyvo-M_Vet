package com.clyvo.veterinary.veterinarian.application.dto;

import com.clyvo.veterinary.veterinarian.domain.model.Specialty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateVeterinarianRequest(
    @NotBlank(message = "CRM is mandatory")
    @Pattern(regexp = "^[0-9]{4,6}-[A-Z]{2}$", message = "CRM inválido (ex: 12345-SP)")
    String crm,

    @NotNull(message = "Specialty is mandatory")
    Specialty specialty,

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    String bio,

    @Pattern(regexp = "^\\(\\d{2}\\)\\s?9?\\d{4}-\\d{4}$", message = "Phone number invalid")
    String phone,

    String profilePictureUrl
) {}

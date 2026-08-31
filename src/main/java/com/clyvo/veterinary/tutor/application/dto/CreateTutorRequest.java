package com.clyvo.veterinary.tutor.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateTutorRequest(
    @NotBlank(message = "Phone is mandatory")
    String phone,

    String address,

    @NotBlank(message = "Document (CPF) is mandatory")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "CPF inválido (ex: 123.456.789-00)")
    String document,

    String profilePictureUrl
) {}

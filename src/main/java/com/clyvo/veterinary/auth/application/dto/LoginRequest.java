package com.clyvo.veterinary.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O formato do email é inválido")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        String password
) {}

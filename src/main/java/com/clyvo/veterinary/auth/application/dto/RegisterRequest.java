package com.clyvo.veterinary.auth.application.dto;

import com.clyvo.veterinary.user.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório")
        String name,
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O formato do email é inválido")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password,
        Role role
) {}

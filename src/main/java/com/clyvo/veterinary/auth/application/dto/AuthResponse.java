package com.clyvo.veterinary.auth.application.dto;

import com.clyvo.veterinary.user.domain.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuthResponse(
        String token,
        String type,
        UUID userId,
        String name,
        String email,
        String role,
        LocalDateTime expiresAt
) {
    public static AuthResponse bearer(String token, User user, LocalDateTime expiresAt) {
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                expiresAt
        );
    }
}

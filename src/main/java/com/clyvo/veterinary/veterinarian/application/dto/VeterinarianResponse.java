package com.clyvo.veterinary.veterinarian.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VeterinarianResponse(
    UUID id,
    UUID userId,
    String name,
    String email,
    String crm,
    String specialty,
    String subscriptionPlan,
    String subscriptionStatus,
    String bio,
    String phone,
    String profilePictureUrl,
    LocalDateTime createdAt
) {}

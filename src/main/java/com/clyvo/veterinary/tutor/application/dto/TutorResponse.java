package com.clyvo.veterinary.tutor.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TutorResponse(
    UUID id,
    UUID userId,
    String name,
    String email,
    String phone,
    String address,
    String document,
    String profilePictureUrl,
    LocalDateTime createdAt
) {}

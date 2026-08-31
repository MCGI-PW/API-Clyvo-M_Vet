package com.clyvo.veterinary.tutor.application.dto;

public record UpdateTutorRequest(
    String phone,
    String address,
    String profilePictureUrl
) {}

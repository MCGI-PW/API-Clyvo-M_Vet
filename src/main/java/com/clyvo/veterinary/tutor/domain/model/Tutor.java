package com.clyvo.veterinary.tutor.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Tutor {
    private UUID id;
    private UUID userId;
    private String phone;
    private String address;
    private String document; 
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Tutor() {}

    public Tutor(UUID id, UUID userId, String phone, String address, String document, String profilePictureUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.phone = phone;
        this.address = address;
        this.document = document;
        this.profilePictureUrl = profilePictureUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Tutor createProfile(UUID userId, String phone, String address, String document) {
        return new Tutor(
            UUID.randomUUID(),
            userId,
            phone,
            address,
            document,
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public void updateProfile(String phone, String address, String profilePictureUrl) {
        if (phone != null) this.phone = phone;
        if (address != null) this.address = address;
        if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDocument() { return document; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

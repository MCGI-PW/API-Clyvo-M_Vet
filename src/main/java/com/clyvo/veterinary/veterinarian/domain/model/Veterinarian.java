package com.clyvo.veterinary.veterinarian.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Veterinarian {
    private UUID id;
    private UUID userId;
    private String crm;
    private Specialty specialty;
    private String bio;
    private String phone;
    private String profilePictureUrl;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Veterinarian() {}

    public Veterinarian(UUID id, UUID userId, String crm, Specialty specialty, String bio, String phone, 
                        String profilePictureUrl, SubscriptionPlan subscriptionPlan, SubscriptionStatus subscriptionStatus, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.crm = crm;
        this.specialty = specialty;
        this.bio = bio;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionStatus = subscriptionStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Veterinarian createProfile(UUID userId, String crm, Specialty specialty, String bio, String phone) {
        return new Veterinarian(
            UUID.randomUUID(),
            userId,
            crm,
            specialty,
            bio,
            phone,
            null,
            SubscriptionPlan.FREE,
            SubscriptionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    public void updateProfile(String bio, String phone, String profilePictureUrl, Specialty specialty) {
        if (bio != null) this.bio = bio;
        if (phone != null) this.phone = phone;
        if (profilePictureUrl != null) this.profilePictureUrl = profilePictureUrl;
        if (specialty != null) this.specialty = specialty;
        this.updatedAt = LocalDateTime.now();
    }

    public void upgradeSubscription(SubscriptionPlan plan) {
        this.subscriptionPlan = plan;
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelSubscription() {
        this.subscriptionPlan = SubscriptionPlan.FREE;
        this.subscriptionStatus = SubscriptionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getCrm() { return crm; }
    public Specialty getSpecialty() { return specialty; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public SubscriptionPlan getSubscriptionPlan() { return subscriptionPlan; }
    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

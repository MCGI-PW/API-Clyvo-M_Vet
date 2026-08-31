package com.clyvo.veterinary.pet.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Pet {
    private UUID id;
    private UUID tutorId;
    private String name;
    private Species species;
    private String breed;
    private LocalDate birthDate;
    private Double weight;
    private String color;
    private String profilePictureUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Pet(UUID id, UUID tutorId, String name, Species species, String breed, LocalDate birthDate, Double weight, String color, String profilePictureUrl, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.tutorId = tutorId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
        this.color = color;
        this.profilePictureUrl = profilePictureUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Pet create(UUID tutorId, String name, Species species, String breed, LocalDate birthDate) {
        return new Pet(UUID.randomUUID(), tutorId, name, species, breed, birthDate, null, null, null, true, LocalDateTime.now(), LocalDateTime.now());
    }

    public void updateInfo(String name, String breed, LocalDate birthDate, Double weight, String color, String profilePictureUrl) {
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
        this.color = color;
        this.profilePictureUrl = profilePictureUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTutorId() { return tutorId; }
    public String getName() { return name; }
    public Species getSpecies() { return species; }
    public String getBreed() { return breed; }
    public LocalDate getBirthDate() { return birthDate; }
    public Double getWeight() { return weight; }
    public String getColor() { return color; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

package com.clyvo.veterinary.user.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String googleId;

    private User(UUID id, String name, String email, String passwordHash, Role role, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt, String googleId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.googleId = googleId;
    }

    public static User create(String name, String email, String passwordHash, Role role) {
        return new User(UUID.randomUUID(), name, email, passwordHash, role, true, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    public static User createWithGoogle(String name, String email, String googleId, Role role) {
        return new User(UUID.randomUUID(), name, email, null, role, true, LocalDateTime.now(), LocalDateTime.now(), googleId);
    }

    public static User load(UUID id, String name, String email, String passwordHash, Role role, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt, String googleId) {
        return new User(id, name, email, passwordHash, role, active, createdAt, updatedAt, googleId);
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateGoogleId(String googleId) {
        this.googleId = googleId;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getGoogleId() { return googleId; }
}

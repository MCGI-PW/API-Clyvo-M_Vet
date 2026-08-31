package com.clyvo.veterinary.user.domain.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_VETERINARIAN,
    ROLE_TUTOR;

    public String getDisplayName() {
        return name().replace("ROLE_", "");
    }
}

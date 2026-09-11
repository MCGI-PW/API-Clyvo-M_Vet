package com.clyvo.veterinary.models.enums;

public enum TipoConta {
    TUTOR,
    VETERINARIO,
    CLINICA;

    public static boolean isValid(String role) {
        if (role == null) return false;
        for (TipoConta t : values()) {
            if (t.name().equalsIgnoreCase(role.trim())) {
                return true;
            }
        }
        return false;
    }
}

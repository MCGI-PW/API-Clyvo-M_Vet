package com.clyvo.veterinary.veterinarian.domain.model;

public enum Specialty {
    GENERAL_PRACTICE("Clínica Geral"),
    SURGERY("Cirurgia"),
    DERMATOLOGY("Dermatologia"),
    CARDIOLOGY("Cardiologia"),
    ONCOLOGY("Oncologia"),
    ORTHOPEDICS("Ortopedia"),
    OPHTHALMOLOGY("Oftalmologia"),
    NEUROLOGY("Neurologia"),
    DENTISTRY("Odontologia"),
    EXOTIC_ANIMALS("Animais Exóticos");

    private final String displayName;

    Specialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

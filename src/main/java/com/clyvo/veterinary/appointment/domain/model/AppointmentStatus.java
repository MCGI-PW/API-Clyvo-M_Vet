package com.clyvo.veterinary.appointment.domain.model;

public enum AppointmentStatus {
    SCHEDULED("Agendado"),
    CONFIRMED("Confirmado"),
    IN_PROGRESS("Em Andamento"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado"),
    NO_SHOW("Não Compareceu");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

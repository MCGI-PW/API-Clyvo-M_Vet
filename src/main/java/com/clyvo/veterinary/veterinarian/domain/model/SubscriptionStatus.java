package com.clyvo.veterinary.veterinarian.domain.model;

public enum SubscriptionStatus {
    ACTIVE("Ativo"),
    INACTIVE("Inativo"),
    PENDING_PAYMENT("Pagamento Pendente"),
    CANCELLED("Cancelado");

    private final String displayName;

    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

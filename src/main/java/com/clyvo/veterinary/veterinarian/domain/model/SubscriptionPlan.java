package com.clyvo.veterinary.veterinarian.domain.model;

public enum SubscriptionPlan {
    FREE("Gratuito", 0),
    BASIC("Básico", 49),
    PREMIUM("Premium", 99);

    private final String displayName;
    private final int monthlyPrice;

    SubscriptionPlan(String displayName, int monthlyPrice) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMonthlyPrice() {
        return monthlyPrice;
    }
}

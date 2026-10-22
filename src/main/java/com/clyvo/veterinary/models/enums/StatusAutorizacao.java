package com.clyvo.veterinary.models.enums;

public enum StatusAutorizacao {
    ATIVA,
    REVOGADA,
    EXPIRADA;

    /** Compara com o valor persistido (String) sem depender de literais espalhados pelo código. */
    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value.trim());
    }
}

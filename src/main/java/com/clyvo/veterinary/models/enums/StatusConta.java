package com.clyvo.veterinary.models.enums;

public enum StatusConta {
    ATIVA,
    BLOQUEADA,
    PENDENTE;

    /** Compara com o valor persistido (String) sem depender de literais espalhados pelo código. */
    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value.trim());
    }
}

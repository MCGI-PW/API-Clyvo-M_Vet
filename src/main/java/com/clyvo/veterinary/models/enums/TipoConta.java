package com.clyvo.veterinary.models.enums;

public enum TipoConta {
    TUTOR("/web/tutor/dashboard"),
    VETERINARIO("/web/vet/dashboard"),
    CLINICA("/web/clinica/dashboard");

    private final String dashboardPath;

    TipoConta(String dashboardPath) {
        this.dashboardPath = dashboardPath;
    }

    /** Rota do painel Web correspondente ao perfil. */
    public String getDashboardPath() {
        return dashboardPath;
    }

    public boolean matches(String value) {
        return value != null && name().equalsIgnoreCase(value.trim());
    }

    public static boolean isValid(String role) {
        return from(role) != null;
    }

    /** Converte o valor persistido para o enum; retorna null quando inválido. */
    public static TipoConta from(String role) {
        if (role == null) return null;
        for (TipoConta t : values()) {
            if (t.matches(role)) {
                return t;
            }
        }
        return null;
    }
}

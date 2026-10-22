package com.clyvo.veterinary.config;

import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.models.enums.TipoConta;
import jakarta.servlet.http.HttpSession;

/**
 * Centraliza os atributos da sessão Web e o redirecionamento por perfil,
 * reutilizados pelo login por formulário, cadastro, OAuth2 real e OAuth2 simulado.
 */
public final class WebSession {

    public static final String TOKEN = "jwtToken";
    public static final String ROLE = "userRole";
    public static final String EMAIL = "userEmail";
    public static final String NAME = "userName";

    private static final String LOGIN_PATH = "/web/login";

    private WebSession() {
    }

    /** Grava os dados de autenticação na sessão e devolve a rota do painel do perfil. */
    public static String start(HttpSession session, AuthResponse auth, String email, String name) {
        session.setAttribute(ROLE, auth.getRole());
        session.setAttribute(TOKEN, auth.getToken());
        session.setAttribute(EMAIL, email);
        if (name != null) {
            session.setAttribute(NAME, name);
        }
        return dashboardPath(auth.getRole());
    }

    public static String dashboardPath(String role) {
        TipoConta tipo = TipoConta.from(role);
        return tipo != null ? tipo.getDashboardPath() : LOGIN_PATH;
    }
}

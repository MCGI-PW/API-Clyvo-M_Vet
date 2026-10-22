package com.clyvo.veterinary.config;

import com.clyvo.veterinary.dto.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class WebSessionTest {

    @Test
    @DisplayName("Deve redirecionar cada perfil para o seu painel")
    void deveResolverPainelPorPerfil() {
        assertEquals("/web/tutor/dashboard", WebSession.dashboardPath("TUTOR"));
        assertEquals("/web/vet/dashboard", WebSession.dashboardPath("veterinario"));
        assertEquals("/web/clinica/dashboard", WebSession.dashboardPath("CLINICA"));
    }

    @Test
    @DisplayName("Perfil ausente ou desconhecido deve voltar para o login")
    void perfilInvalidoDeveVoltarAoLogin() {
        assertEquals("/web/login", WebSession.dashboardPath(null));
        assertEquals("/web/login", WebSession.dashboardPath("ADMIN"));
    }

    @Test
    @DisplayName("Deve gravar token, perfil e e-mail na sessão ao iniciar")
    void deveIniciarSessao() {
        MockHttpSession session = new MockHttpSession();

        String destino = WebSession.start(session, new AuthResponse("jwt", "TUTOR"), "ana@email.com", "Ana");

        assertEquals("/web/tutor/dashboard", destino);
        assertEquals("jwt", session.getAttribute(WebSession.TOKEN));
        assertEquals("TUTOR", session.getAttribute(WebSession.ROLE));
        assertEquals("ana@email.com", session.getAttribute(WebSession.EMAIL));
        assertEquals("Ana", session.getAttribute(WebSession.NAME));
    }
}

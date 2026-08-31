package com.clyvo.veterinary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de sanidade da aplicação.
 * Verifica que o contexto Spring Boot carrega sem erros.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ClyvoVeterinaryApplication — Teste de Contexto")
class ClyvoVeterinaryApplicationTest {

    @Test
    @DisplayName("contextLoads — o contexto Spring deve inicializar corretamente")
    void contextLoads() {
        // Se chegar aqui sem exceção, o contexto foi carregado com sucesso
    }
}

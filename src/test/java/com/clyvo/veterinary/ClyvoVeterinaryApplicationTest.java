package com.clyvo.veterinary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("ClyvoVeterinaryApplication — Teste de Contexto")
class ClyvoVeterinaryApplicationTest {

    @Test
    @DisplayName("contextLoads — o contexto Spring deve inicializar corretamente")
    void contextLoads() {
    }
}

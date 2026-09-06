package com.clyvo.veterinary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ClyvoVeterinaryApplicationTests {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação com sucesso no perfil de teste")
    void contextLoads() {
    }

}

package com.clyvo.veterinary.services.ia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Valida a extração por regras usada quando não há LLM disponível. */
class AssistenteClinicoLocalTest {

    private final AssistenteClinicoLocal assistente = new AssistenteClinicoLocal();

    private static final String TRANSCRICAO = """
            Bom dia, o Thor está coçando muito a orelha desde sábado.
            Pelo exame parece ser uma otite externa bacteriana.
            Vou prescrever uma solução otológica, 3 gotas duas vezes ao dia por 7 dias.
            Mantenha a orelha seca e evite banho nesse período.
            Se ele tiver febre ou parar de comer, procure atendimento imediatamente.
            Retorno em 10 dias para reavaliação.
            """;

    @Test
    @DisplayName("Separa diagnóstico, prescrição, orientações e sinais de alerta da transcrição")
    void classificaFrasesDaConsulta() {
        ResumoClinico resumo = assistente.resumir(TRANSCRICAO, "Thor (Golden Retriever)");

        assertTrue(resumo.diagnostico().contains("otite externa"));
        assertTrue(resumo.prescricao().contains("3 gotas"));
        assertTrue(resumo.orientacoes().contains("Retorno em 10 dias"));
        assertTrue(resumo.orientacoes().contains("orelha seca"));
        assertEquals(1, resumo.sinaisDeAlerta().size());
        assertTrue(resumo.sinaisDeAlerta().get(0).contains("febre"));
        assertTrue(resumo.resumoTutor().startsWith("Consulta de Thor"));
    }

    @Test
    @DisplayName("Não inventa conteúdo quando a transcrição não traz informação clínica")
    void naoInventaQuandoNaoHaConteudoClinico() {
        ResumoClinico resumo = assistente.resumir("Olá, tudo bem? Pode me ouvir?", "Mel");

        assertEquals("", resumo.diagnostico());
        assertEquals("", resumo.prescricao());
        assertTrue(resumo.sinaisDeAlerta().isEmpty());
    }
}

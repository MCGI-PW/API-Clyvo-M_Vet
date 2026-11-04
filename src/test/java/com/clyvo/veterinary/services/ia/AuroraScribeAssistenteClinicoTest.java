package com.clyvo.veterinary.services.ia;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** Integração HTTP com o AuroraScribe AI, simulado por um servidor local dentro do teste. */
class AuroraScribeAssistenteClinicoTest {

    private HttpServer servidor;

    @AfterEach
    void pararServidor() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    private String subirAuroraScribe(int status, String resposta, AtomicReference<String> corpoRecebido) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/api/transcriptions/summarize", troca -> {
            corpoRecebido.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
            troca.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = troca.getResponseBody()) {
                out.write(bytes);
            }
        });
        servidor.start();
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/";
    }

    @Test
    @DisplayName("Converte a resposta do AuroraScribe no resumo clínico, incluindo alertas de segurança")
    void converteRespostaDoServico() throws IOException {
        AtomicReference<String> recebido = new AtomicReference<>();
        String url = subirAuroraScribe(200, """
                {"diagnostico": "Otite externa", "prescricao": "Gotas otológicas — a cada 12h — por 7 dias",
                 "orientacoes": "Manter a orelha seca", "resumo_tutor": "O Thor está com otite.",
                 "sinais_alerta": ["Febre"], "alertas_clinicos": ["Paciente braquicefálico"], "fonte": "LOCAL:extracao"}
                """, recebido);

        ResumoClinico resumo = new AuroraScribeAssistenteClinico(url).resumir("transcrição da consulta", "Thor (Buldogue Francês)");

        assertEquals("Otite externa", resumo.diagnostico());
        assertTrue(resumo.prescricao().contains("a cada 12h"));
        assertEquals("O Thor está com otite.", resumo.resumoTutor());
        assertEquals(2, resumo.sinaisDeAlerta().size());
        assertTrue(resumo.sinaisDeAlerta().get(1).contains("braquicefálico"));
        assertTrue(recebido.get().contains("\"contexto_paciente\":\"Thor (Buldogue Francês)\""));
    }

    @Test
    @DisplayName("Erro HTTP do AuroraScribe vira exceção para o serviço usar o assistente local")
    void erroDoServicoLancaExcecao() throws IOException {
        String url = subirAuroraScribe(500, "{\"error\": \"falha\"}", new AtomicReference<>());

        AuroraScribeAssistenteClinico assistente = new AuroraScribeAssistenteClinico(url);

        assertThrows(IllegalStateException.class, () -> assistente.resumir("texto", "Thor"));
    }

    @Test
    @DisplayName("Serviço fora do ar também cai no assistente local")
    void servicoForaDoAr() {
        AuroraScribeAssistenteClinico assistente = new AuroraScribeAssistenteClinico("http://127.0.0.1:1");

        assertThrows(IllegalStateException.class, () -> assistente.resumir("texto", "Thor"));
    }
}

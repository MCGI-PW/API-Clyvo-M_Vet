package com.clyvo.veterinary.services.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resumo clínico feito pelo ClyvoScribe AI, o serviço de IA da CLYVO VET em Python
 * (repositório IoT-Clyvo-Vet_M). Chama {@code POST /api/transcriptions/summarize}; lá dentro o
 * ClyvoScribe usa o Claude ou o seu extrator local e ainda aplica as regras de segurança clínica.
 */
public class ClyvoScribeAssistenteClinico implements AssistenteClinico {

    private static final Duration TIMEOUT = Duration.ofSeconds(90);

    private final String endpoint;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    public ClyvoScribeAssistenteClinico(String urlBase) {
        String base = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.endpoint = base + "/api/transcriptions/summarize";
    }

    @Override
    public ResumoClinico resumir(String transcricao, String contextoPaciente) {
        try {
            String corpo = mapper.writeValueAsString(Map.of(
                    "transcript", transcricao,
                    "contexto_paciente", contextoPaciente));
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(corpo))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("ClyvoScribe respondeu HTTP " + response.statusCode());
            }

            JsonNode json = mapper.readTree(response.body());
            List<String> sinais = new ArrayList<>();
            json.path("sinais_alerta").forEach(s -> sinais.add(s.asText()));
            // Alertas da IA simbólica (ex.: raça sensível a ivermectina) também precisam chegar ao veterinário
            json.path("alertas_clinicos").forEach(a -> sinais.add("⚠ " + a.asText()));

            return new ResumoClinico(
                    json.path("diagnostico").asText(""),
                    json.path("prescricao").asText(""),
                    json.path("orientacoes").asText(""),
                    json.path("resumo_tutor").asText(""),
                    sinais);
        } catch (IOException e) {
            // Quem chama trata a falha e usa o assistente local: a consulta não trava
            throw new IllegalStateException("ClyvoScribe indisponível: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chamada ao ClyvoScribe interrompida.", e);
        }
    }

    @Override
    public String fonte() {
        return "CLYVOSCRIBE";
    }
}

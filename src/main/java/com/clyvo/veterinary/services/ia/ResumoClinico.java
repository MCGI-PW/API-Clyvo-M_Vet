package com.clyvo.veterinary.services.ia;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * Formato estruturado que a IA devolve. O mesmo record gera o JSON Schema enviado ao Claude
 * (structured outputs), então a resposta já chega validada e tipada.
 */
public record ResumoClinico(
        @JsonPropertyDescription("Diagnóstico ou hipótese diagnóstica mencionada pelo veterinário. Vazio se não foi dito.")
        String diagnostico,
        @JsonPropertyDescription("Medicamentos, doses, frequência e duração prescritos. Vazio se não houve prescrição.")
        String prescricao,
        @JsonPropertyDescription("Orientações de cuidado em casa e data/condição de retorno.")
        String orientacoes,
        @JsonPropertyDescription("Resumo da consulta em linguagem simples para o tutor, em até 4 frases, sem jargão.")
        String resumoTutor,
        @JsonPropertyDescription("Sinais de alerta citados na consulta que exigem retorno imediato.")
        List<String> sinaisDeAlerta) {
}

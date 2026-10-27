package com.clyvo.veterinary.dto;

import com.clyvo.veterinary.models.TranscricaoConsulta;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Sugestões da IA devolvidas à interface para o veterinário revisar e confirmar. */
public record ResumoConsultaResponse(
        UUID idTranscricao,
        UUID idConsulta,
        String diagnostico,
        String prescricao,
        String orientacoes,
        String resumoTutor,
        List<String> sinaisDeAlerta,
        String fonte,
        LocalDateTime geradoEm) {

    public static ResumoConsultaResponse of(TranscricaoConsulta t) {
        List<String> alertas = t.getSinaisAlerta() == null || t.getSinaisAlerta().isBlank()
                ? List.of()
                : Arrays.stream(t.getSinaisAlerta().split("\n")).filter(s -> !s.isBlank()).toList();
        return new ResumoConsultaResponse(t.getIdTranscricao(), t.getConsulta().getIdConsulta(),
                t.getDiagnosticoSugerido(), t.getPrescricaoSugerida(), t.getOrientacoesSugeridas(),
                t.getResumoTutor(), alertas, t.getFonteIa(), t.getDataGeracao());
    }
}

package com.clyvo.veterinary.services.ia;

/** Transforma a transcrição de uma consulta em um resumo clínico estruturado. */
public interface AssistenteClinico {

    ResumoClinico resumir(String transcricao, String contextoPaciente);

    /** Identifica quem gerou o resumo (gravado junto da transcrição para auditoria). */
    String fonte();
}

package com.clyvo.veterinary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Texto da consulta: transcrição ao vivo do navegador ou transcrição colada de uma gravação. */
public class TranscricaoRequest {
    @NotBlank(message = "A transcrição da consulta é obrigatória.")
    @Size(max = 50000, message = "A transcrição pode ter no máximo 50.000 caracteres.")
    private String transcript;
    public String getTranscript() { return transcript; } public void setTranscript(String transcript) { this.transcript = transcript; }
}

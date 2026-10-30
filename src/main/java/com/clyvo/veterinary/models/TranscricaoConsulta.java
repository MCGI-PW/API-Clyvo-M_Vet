package com.clyvo.veterinary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** Transcrição de uma teleconsulta e as sugestões geradas pela IA (o veterinário revisa antes de virar prontuário). */
@Entity
@Table(name = "transcricao_consulta")
public class TranscricaoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_transcricao")
    private UUID idTranscricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Consulta consulta;

    @Lob
    @Column(name = "texto_transcricao", nullable = false)
    private String textoTranscricao;

    @Column(name = "diagnostico_sugerido", length = 4000)
    private String diagnosticoSugerido;

    @Column(name = "prescricao_sugerida", length = 4000)
    private String prescricaoSugerida;

    @Column(name = "orientacoes_sugeridas", length = 4000)
    private String orientacoesSugeridas;

    @Column(name = "resumo_tutor", length = 4000)
    private String resumoTutor;

    @Column(name = "sinais_alerta", length = 4000)
    private String sinaisAlerta;

    @Column(name = "fonte_ia", nullable = false)
    private String fonteIa;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao = LocalDateTime.now();

    public UUID getIdTranscricao() { return idTranscricao; } public void setIdTranscricao(UUID id) { this.idTranscricao = id; }
    public Consulta getConsulta() { return consulta; } public void setConsulta(Consulta c) { this.consulta = c; }
    public String getTextoTranscricao() { return textoTranscricao; } public void setTextoTranscricao(String t) { this.textoTranscricao = t; }
    public String getDiagnosticoSugerido() { return diagnosticoSugerido; } public void setDiagnosticoSugerido(String d) { this.diagnosticoSugerido = d; }
    public String getPrescricaoSugerida() { return prescricaoSugerida; } public void setPrescricaoSugerida(String p) { this.prescricaoSugerida = p; }
    public String getOrientacoesSugeridas() { return orientacoesSugeridas; } public void setOrientacoesSugeridas(String o) { this.orientacoesSugeridas = o; }
    public String getResumoTutor() { return resumoTutor; } public void setResumoTutor(String r) { this.resumoTutor = r; }
    public String getSinaisAlerta() { return sinaisAlerta; } public void setSinaisAlerta(String s) { this.sinaisAlerta = s; }
    public String getFonteIa() { return fonteIa; } public void setFonteIa(String f) { this.fonteIa = f; }
    public LocalDateTime getDataGeracao() { return dataGeracao; } public void setDataGeracao(LocalDateTime d) { this.dataGeracao = d; }
}

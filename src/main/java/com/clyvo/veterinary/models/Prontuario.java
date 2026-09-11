package com.clyvo.veterinary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prontuario")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_prontuario")
    private UUID idProntuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consulta", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Consulta consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Veterinario veterinario;

    @Column(name = "notas_clinicas", length = 4000, nullable = false)
    private String notasClinicas;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

    public UUID getIdProntuario() {
        return idProntuario;
    }

    public void setIdProntuario(UUID idProntuario) {
        this.idProntuario = idProntuario;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    public String getNotasClinicas() {
        return notasClinicas;
    }

    public void setNotasClinicas(String notasClinicas) {
        this.notasClinicas = notasClinicas;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}

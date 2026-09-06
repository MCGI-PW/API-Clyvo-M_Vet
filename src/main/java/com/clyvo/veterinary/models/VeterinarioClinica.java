package com.clyvo.veterinary.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "veterinario_clinica")
public class VeterinarioClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_veterinario_clinica")
    private UUID idVeterinarioClinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Veterinario veterinario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clinica", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Clinica clinica;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio = LocalDate.now();

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "status_vinculo", nullable = false)
    private String statusVinculo = "ATIVO";

    public UUID getIdVeterinarioClinica() {
        return idVeterinarioClinica;
    }

    public void setIdVeterinarioClinica(UUID idVeterinarioClinica) {
        this.idVeterinarioClinica = idVeterinarioClinica;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    public Clinica getClinica() {
        return clinica;
    }

    public void setClinica(Clinica clinica) {
        this.clinica = clinica;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getStatusVinculo() {
        return statusVinculo;
    }

    public void setStatusVinculo(String statusVinculo) {
        this.statusVinculo = statusVinculo;
    }
}

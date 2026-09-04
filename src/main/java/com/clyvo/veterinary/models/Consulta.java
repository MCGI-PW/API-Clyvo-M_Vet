package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consulta")
public class Consulta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_consulta")
    private UUID idConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pet", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Veterinario veterinario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private String modalidade;

    @Column(nullable = false)
    private String status = "AGENDADO";

    public UUID getIdConsulta() { return idConsulta; } public void setIdConsulta(UUID id) { this.idConsulta = id; }
    public Pet getPet() { return pet; } public void setPet(Pet p) { this.pet = p; }
    public Veterinario getVeterinario() { return veterinario; } public void setVeterinario(Veterinario v) { this.veterinario = v; }
    public LocalDateTime getDataHora() { return dataHora; } public void setDataHora(LocalDateTime d) { this.dataHora = d; }
    public String getModalidade() { return modalidade; } public void setModalidade(String m) { this.modalidade = m; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
}

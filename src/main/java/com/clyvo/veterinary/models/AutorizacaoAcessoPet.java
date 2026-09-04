package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "autorizacao_acesso_pet")
public class AutorizacaoAcessoPet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_autorizacao")
    private UUID idAutorizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pet", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta_autorizador", nullable = false)
    private ContaAcesso contaAutorizador;

    @Column(name = "data_autorizacao")
    private LocalDateTime dataAutorizacao = LocalDateTime.now();

    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    @Column(nullable = false)
    private String status;

    @Column(name = "motivo_revogacao")
    private String motivoRevogacao;

    // Getters and setters
    public UUID getIdAutorizacao() { return idAutorizacao; } public void setIdAutorizacao(UUID id) { this.idAutorizacao = id; }
    public Pet getPet() { return pet; } public void setPet(Pet p) { this.pet = p; }
    public Veterinario getVeterinario() { return veterinario; } public void setVeterinario(Veterinario v) { this.veterinario = v; }
    public ContaAcesso getContaAutorizador() { return contaAutorizador; } public void setContaAutorizador(ContaAcesso c) { this.contaAutorizador = c; }
    public LocalDateTime getDataAutorizacao() { return dataAutorizacao; } public void setDataAutorizacao(LocalDateTime d) { this.dataAutorizacao = d; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; } public void setDataExpiracao(LocalDateTime d) { this.dataExpiracao = d; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getMotivoRevogacao() { return motivoRevogacao; } public void setMotivoRevogacao(String m) { this.motivoRevogacao = m; }
}

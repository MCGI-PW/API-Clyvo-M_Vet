package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conta_acesso")
public class ContaAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_conta")
    private UUID idConta;

    @Column(name = "tipo_conta")
    private String tipoConta;

    @Column(unique = true, nullable = false)
    private String email;

    private String telefone;

    @Column(name = "status_conta")
    private String statusConta;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() { dataCriacao = LocalDateTime.now(); dataAtualizacao = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { dataAtualizacao = LocalDateTime.now(); }

    // Getters and Setters
    public UUID getIdConta() { return idConta; } public void setIdConta(UUID id) { this.idConta = id; }
    public String getTipoConta() { return tipoConta; } public void setTipoConta(String t) { this.tipoConta = t; }
    public String getEmail() { return email; } public void setEmail(String e) { this.email = e; }
    public String getTelefone() { return telefone; } public void setTelefone(String t) { this.telefone = t; }
    public String getStatusConta() { return statusConta; } public void setStatusConta(String s) { this.statusConta = s; }
    public LocalDateTime getDataCriacao() { return dataCriacao; } public void setDataCriacao(LocalDateTime d) { this.dataCriacao = d; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; } public void setDataAtualizacao(LocalDateTime d) { this.dataAtualizacao = d; }
}

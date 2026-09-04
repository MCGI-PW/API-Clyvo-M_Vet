package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credencial")
public class Credencial {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_credencial")
    private UUID idCredencial;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false, unique = true)
    private ContaAcesso contaAcesso;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "data_ultima_alteracao")
    private LocalDateTime dataUltimaAlteracao = LocalDateTime.now();

    @Column(name = "tentativas_falhas")
    private Integer tentativasFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "troca_senha_obrigatoria")
    private Boolean trocaSenhaObrigatoria = false;

    // Getters and setters
    public UUID getIdCredencial() { return idCredencial; } public void setIdCredencial(UUID id) { this.idCredencial = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getSenhaHash() { return senhaHash; } public void setSenhaHash(String s) { this.senhaHash = s; }
    public LocalDateTime getDataUltimaAlteracao() { return dataUltimaAlteracao; } public void setDataUltimaAlteracao(LocalDateTime d) { this.dataUltimaAlteracao = d; }
    public Integer getTentativasFalhas() { return tentativasFalhas; } public void setTentativasFalhas(Integer t) { this.tentativasFalhas = t; }
    public LocalDateTime getBloqueadoAte() { return bloqueadoAte; } public void setBloqueadoAte(LocalDateTime b) { this.bloqueadoAte = b; }
    public Boolean getTrocaSenhaObrigatoria() { return trocaSenhaObrigatoria; } public void setTrocaSenhaObrigatoria(Boolean t) { this.trocaSenhaObrigatoria = t; }
}

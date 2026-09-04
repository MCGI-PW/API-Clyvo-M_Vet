package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessao")
public class Sessao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_sessao")
    private UUID idSessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaAcesso contaAcesso;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_revogacao")
    private LocalDateTime dataRevogacao;

    private String ip;
    
    @Column(name = "user_agent")
    private String userAgent;

    // Getters and setters
    public UUID getIdSessao() { return idSessao; } public void setIdSessao(UUID id) { this.idSessao = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getTokenHash() { return tokenHash; } public void setTokenHash(String t) { this.tokenHash = t; }
    public LocalDateTime getDataCriacao() { return dataCriacao; } public void setDataCriacao(LocalDateTime d) { this.dataCriacao = d; }
    public LocalDateTime getDataExpiracao() { return dataExpiracao; } public void setDataExpiracao(LocalDateTime d) { this.dataExpiracao = d; }
    public LocalDateTime getDataRevogacao() { return dataRevogacao; } public void setDataRevogacao(LocalDateTime d) { this.dataRevogacao = d; }
    public String getIp() { return ip; } public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; } public void setUserAgent(String u) { this.userAgent = u; }
}

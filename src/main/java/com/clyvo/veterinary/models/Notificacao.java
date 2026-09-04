package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificacao")
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_notificacao")
    private UUID idNotificacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ContaAcesso contaAcesso;

    @Column(nullable = false)
    private String mensagem;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(nullable = false)
    private boolean lida = false;

    public UUID getIdNotificacao() { return idNotificacao; } public void setIdNotificacao(UUID id) { this.idNotificacao = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getMensagem() { return mensagem; } public void setMensagem(String m) { this.mensagem = m; }
    public LocalDateTime getDataCriacao() { return dataCriacao; } public void setDataCriacao(LocalDateTime d) { this.dataCriacao = d; }
    public boolean isLida() { return lida; } public void setLida(boolean l) { this.lida = l; }
}

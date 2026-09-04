package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conta_perfil")
public class ContaPerfil {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_conta_perfil")
    private UUID idContaPerfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaAcesso contaAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;

    private Boolean ativo = true;

    @Column(name = "data_atribuicao")
    private LocalDateTime dataAtribuicao = LocalDateTime.now();

    // Getters and setters
    public UUID getIdContaPerfil() { return idContaPerfil; } public void setIdContaPerfil(UUID id) { this.idContaPerfil = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public Perfil getPerfil() { return perfil; } public void setPerfil(Perfil p) { this.perfil = p; }
    public Boolean getAtivo() { return ativo; } public void setAtivo(Boolean a) { this.ativo = a; }
    public LocalDateTime getDataAtribuicao() { return dataAtribuicao; } public void setDataAtribuicao(LocalDateTime d) { this.dataAtribuicao = d; }
}

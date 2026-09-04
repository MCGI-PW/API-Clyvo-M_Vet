package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "identificador_acesso")
public class IdentificadorAcesso {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_identificador")
    private UUID idIdentificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false)
    private ContaAcesso contaAcesso;

    @Column(name = "tipo_identificador", nullable = false)
    private String tipoIdentificador;

    @Column(name = "valor_hash", nullable = false)
    private String valorHash;

    @Column(name = "valor_criptografado")
    private String valorCriptografado;

    private Boolean ativo = true;

    // Getters and setters
    public UUID getIdIdentificador() { return idIdentificador; } public void setIdIdentificador(UUID id) { this.idIdentificador = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getTipoIdentificador() { return tipoIdentificador; } public void setTipoIdentificador(String t) { this.tipoIdentificador = t; }
    public String getValorHash() { return valorHash; } public void setValorHash(String v) { this.valorHash = v; }
    public String getValorCriptografado() { return valorCriptografado; } public void setValorCriptografado(String v) { this.valorCriptografado = v; }
    public Boolean getAtivo() { return ativo; } public void setAtivo(Boolean a) { this.ativo = a; }
}

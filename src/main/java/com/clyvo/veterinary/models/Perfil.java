package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "perfil")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_perfil")
    private UUID idPerfil;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;
    private Boolean ativo = true;

    // Getters and setters
    public UUID getIdPerfil() { return idPerfil; } public void setIdPerfil(UUID id) { this.idPerfil = id; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public String getDescricao() { return descricao; } public void setDescricao(String d) { this.descricao = d; }
    public Boolean getAtivo() { return ativo; } public void setAtivo(Boolean a) { this.ativo = a; }
}

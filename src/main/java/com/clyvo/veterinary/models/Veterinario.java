package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "veterinario")
public class Veterinario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_veterinario")
    private UUID idVeterinario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false, unique = true)
    private ContaAcesso contaAcesso;

    @Column(nullable = false)
    private String nome;

    private String especialidade;

    @Column(name = "situacao_profissional")
    private String situacaoProfissional;

    // Getters and setters
    public UUID getIdVeterinario() { return idVeterinario; } public void setIdVeterinario(UUID id) { this.idVeterinario = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public String getEspecialidade() { return especialidade; } public void setEspecialidade(String e) { this.especialidade = e; }
    public String getSituacaoProfissional() { return situacaoProfissional; } public void setSituacaoProfissional(String s) { this.situacaoProfissional = s; }
}

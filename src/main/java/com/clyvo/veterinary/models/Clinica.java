package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "clinica")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Clinica {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_clinica")
    private UUID idClinica;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ContaAcesso contaAcesso;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false)
    private String nomeFantasia;

    private String endereco;
    private String telefone;
    private Boolean ativa = true;

    // Getters and setters
    public UUID getIdClinica() { return idClinica; } public void setIdClinica(UUID id) { this.idClinica = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getRazaoSocial() { return razaoSocial; } public void setRazaoSocial(String r) { this.razaoSocial = r; }
    public String getNomeFantasia() { return nomeFantasia; } public void setNomeFantasia(String n) { this.nomeFantasia = n; }
    public String getEndereco() { return endereco; } public void setEndereco(String e) { this.endereco = e; }
    public String getTelefone() { return telefone; } public void setTelefone(String t) { this.telefone = t; }
    public Boolean getAtiva() { return ativa; } public void setAtiva(Boolean a) { this.ativa = a; }
}

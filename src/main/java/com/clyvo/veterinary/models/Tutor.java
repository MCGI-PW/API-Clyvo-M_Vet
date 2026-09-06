package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tutor")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Tutor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tutor")
    private UUID idTutor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ContaAcesso contaAcesso;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // Getters and setters
    public UUID getIdTutor() { return idTutor; } public void setIdTutor(UUID id) { this.idTutor = id; }
    public ContaAcesso getContaAcesso() { return contaAcesso; } public void setContaAcesso(ContaAcesso c) { this.contaAcesso = c; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate d) { this.dataNascimento = d; }
}

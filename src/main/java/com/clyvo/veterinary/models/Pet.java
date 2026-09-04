package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pet")
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_pet")
    private UUID idPet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tutor", nullable = false)
    private Tutor tutor;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String sexo;
    private String especie;
    private String raca;
    private Boolean ativo = true;

    // Getters and setters
    public UUID getIdPet() { return idPet; } public void setIdPet(UUID id) { this.idPet = id; }
    public Tutor getTutor() { return tutor; } public void setTutor(Tutor t) { this.tutor = t; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
    public LocalDate getDataNascimento() { return dataNascimento; } public void setDataNascimento(LocalDate d) { this.dataNascimento = d; }
    public String getSexo() { return sexo; } public void setSexo(String s) { this.sexo = s; }
    public String getEspecie() { return especie; } public void setEspecie(String e) { this.especie = e; }
    public String getRaca() { return raca; } public void setRaca(String r) { this.raca = r; }
    public Boolean getAtivo() { return ativo; } public void setAtivo(Boolean a) { this.ativo = a; }
}

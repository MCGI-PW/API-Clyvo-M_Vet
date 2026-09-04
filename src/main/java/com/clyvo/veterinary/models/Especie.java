package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "especie")
public class Especie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_especie")
    private UUID idEspecie;

    @Column(nullable = false, unique = true)
    private String nome;

    public UUID getIdEspecie() { return idEspecie; } public void setIdEspecie(UUID id) { this.idEspecie = id; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
}

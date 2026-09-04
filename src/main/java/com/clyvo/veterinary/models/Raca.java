package com.clyvo.veterinary.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "raca")
public class Raca {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_raca")
    private UUID idRaca;

    @ManyToOne
    @JoinColumn(name = "id_especie", nullable = false)
    private Especie especie;

    @Column(nullable = false)
    private String nome;

    public UUID getIdRaca() { return idRaca; } public void setIdRaca(UUID id) { this.idRaca = id; }
    public Especie getEspecie() { return especie; } public void setEspecie(Especie e) { this.especie = e; }
    public String getNome() { return nome; } public void setNome(String n) { this.nome = n; }
}

package com.clyvo.veterinary.models;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "pets")
public class Pet {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private Integer age;
    @Column(nullable = false) private String breed;
    @ManyToOne @JoinColumn(name = "tutor_id", nullable = false) private Tutor tutor;
    
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; } public void setAge(Integer age) { this.age = age; }
    public String getBreed() { return breed; } public void setBreed(String breed) { this.breed = breed; }
    public Tutor getTutor() { return tutor; } public void setTutor(Tutor tutor) { this.tutor = tutor; }
}

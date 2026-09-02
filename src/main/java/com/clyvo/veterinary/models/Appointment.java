package com.clyvo.veterinary.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private LocalDateTime appointmentDate;
    @Column(nullable = false) private String modality; // ONLINE, PRESENCIAL
    @Column(nullable = false) private String status; // SCHEDULED, COMPLETED, CANCELED
    @Column(columnDefinition = "TEXT") private String clinicalNotes;
    
    @ManyToOne @JoinColumn(name = "veterinarian_id", nullable = false) private Veterinarian veterinarian;
    @ManyToOne @JoinColumn(name = "tutor_id", nullable = false) private Tutor tutor;
    @ManyToOne @JoinColumn(name = "pet_id", nullable = false) private Pet pet;
    
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; } public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getModality() { return modality; } public void setModality(String modality) { this.modality = modality; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getClinicalNotes() { return clinicalNotes; } public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }
    public Veterinarian getVeterinarian() { return veterinarian; } public void setVeterinarian(Veterinarian veterinarian) { this.veterinarian = veterinarian; }
    public Tutor getTutor() { return tutor; } public void setTutor(Tutor tutor) { this.tutor = tutor; }
    public Pet getPet() { return pet; } public void setPet(Pet pet) { this.pet = pet; }
}

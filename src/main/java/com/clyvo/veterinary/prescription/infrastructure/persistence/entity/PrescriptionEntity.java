package com.clyvo.veterinary.prescription.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Entity
@Table(name = "prescriptions")
public class PrescriptionEntity {
    @Id
    private UUID id;
    
    @Column(name = "medical_record_id")
    private UUID medicalRecordId;
    
    @Column(name = "pet_id")
    private UUID petId;
    
    @Column(name = "veterinarian_id")
    private UUID veterinarianId;
    
    @Column(name = "general_instructions", columnDefinition = "TEXT")
    private String generalInstructions;
    
    @Column(name = "valid_until")
    private LocalDate validUntil;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prescription_items", joinColumns = @JoinColumn(name = "prescription_id"))
    private List<PrescriptionItemEmbeddable> medications = new ArrayList<>();

    public PrescriptionEntity() {
    }

    public PrescriptionEntity(UUID id, UUID medicalRecordId, UUID petId, UUID veterinarianId, String generalInstructions, LocalDate validUntil, LocalDateTime createdAt, List<PrescriptionItemEmbeddable> medications) {
        this.id = id;
        this.medicalRecordId = medicalRecordId;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.generalInstructions = generalInstructions;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
        if (medications != null) {
            this.medications = medications;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(UUID medicalRecordId) { this.medicalRecordId = medicalRecordId; }
    
    public UUID getPetId() { return petId; }
    public void setPetId(UUID petId) { this.petId = petId; }
    
    public UUID getVeterinarianId() { return veterinarianId; }
    public void setVeterinarianId(UUID veterinarianId) { this.veterinarianId = veterinarianId; }
    
    public String getGeneralInstructions() { return generalInstructions; }
    public void setGeneralInstructions(String generalInstructions) { this.generalInstructions = generalInstructions; }
    
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<PrescriptionItemEmbeddable> getMedications() { return medications; }
    public void setMedications(List<PrescriptionItemEmbeddable> medications) { this.medications = medications; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptionEntity that = (PrescriptionEntity) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(medicalRecordId, that.medicalRecordId) &&
               Objects.equals(petId, that.petId) &&
               Objects.equals(veterinarianId, that.veterinarianId) &&
               Objects.equals(generalInstructions, that.generalInstructions) &&
               Objects.equals(validUntil, that.validUntil) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(medications, that.medications);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, medicalRecordId, petId, veterinarianId, generalInstructions, validUntil, createdAt, medications);
    }

    @Override
    public String toString() {
        return "PrescriptionEntity{" +
                "id=" + id +
                ", medicalRecordId=" + medicalRecordId +
                ", petId=" + petId +
                ", veterinarianId=" + veterinarianId +
                ", generalInstructions='" + generalInstructions + '\'' +
                ", validUntil=" + validUntil +
                ", createdAt=" + createdAt +
                ", medications=" + medications +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID medicalRecordId;
        private UUID petId;
        private UUID veterinarianId;
        private String generalInstructions;
        private LocalDate validUntil;
        private LocalDateTime createdAt;
        private List<PrescriptionItemEmbeddable> medications = new ArrayList<>();

        private Builder() {}

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder medicalRecordId(UUID medicalRecordId) { this.medicalRecordId = medicalRecordId; return this; }
        public Builder petId(UUID petId) { this.petId = petId; return this; }
        public Builder veterinarianId(UUID veterinarianId) { this.veterinarianId = veterinarianId; return this; }
        public Builder generalInstructions(String generalInstructions) { this.generalInstructions = generalInstructions; return this; }
        public Builder validUntil(LocalDate validUntil) { this.validUntil = validUntil; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder medications(List<PrescriptionItemEmbeddable> medications) { 
            if (medications != null) {
                this.medications = medications;
            }
            return this; 
        }

        public PrescriptionEntity build() {
            return new PrescriptionEntity(id, medicalRecordId, petId, veterinarianId, generalInstructions, validUntil, createdAt, medications);
        }
    }
}

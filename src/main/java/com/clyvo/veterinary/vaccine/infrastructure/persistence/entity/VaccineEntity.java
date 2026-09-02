package com.clyvo.veterinary.vaccine.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vaccines")
public class VaccineEntity {
    
    @Id
    private UUID id;
    
    @Column(name = "pet_id", nullable = false)
    private UUID petId;
    
    @Column(name = "vaccine_name", nullable = false)
    private String vaccineName;
    
    private String manufacturer;
    
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;
    
    @Column(name = "next_dose_at")
    private LocalDate nextDoseAt;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public VaccineEntity() {
    }

    public VaccineEntity(UUID id, UUID petId, String vaccineName, String manufacturer, String batchNumber, LocalDate appliedAt, LocalDate nextDoseAt, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.petId = petId;
        this.vaccineName = vaccineName;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.appliedAt = appliedAt;
        this.nextDoseAt = nextDoseAt;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPetId() { return petId; }
    public void setPetId(UUID petId) { this.petId = petId; }

    public String getVaccineName() { return vaccineName; }
    public void setVaccineName(String vaccineName) { this.vaccineName = vaccineName; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDate appliedAt) { this.appliedAt = appliedAt; }

    public LocalDate getNextDoseAt() { return nextDoseAt; }
    public void setNextDoseAt(LocalDate nextDoseAt) { this.nextDoseAt = nextDoseAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID petId;
        private String vaccineName;
        private String manufacturer;
        private String batchNumber;
        private LocalDate appliedAt;
        private LocalDate nextDoseAt;
        private String notes;
        private LocalDateTime createdAt;

        private Builder() {}

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder petId(UUID petId) { this.petId = petId; return this; }
        public Builder vaccineName(String vaccineName) { this.vaccineName = vaccineName; return this; }
        public Builder manufacturer(String manufacturer) { this.manufacturer = manufacturer; return this; }
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder appliedAt(LocalDate appliedAt) { this.appliedAt = appliedAt; return this; }
        public Builder nextDoseAt(LocalDate nextDoseAt) { this.nextDoseAt = nextDoseAt; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public VaccineEntity build() {
            return new VaccineEntity(id, petId, vaccineName, manufacturer, batchNumber, appliedAt, nextDoseAt, notes, createdAt);
        }
    }
}

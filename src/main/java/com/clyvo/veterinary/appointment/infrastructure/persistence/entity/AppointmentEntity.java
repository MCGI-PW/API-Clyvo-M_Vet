package com.clyvo.veterinary.appointment.infrastructure.persistence.entity;

import com.clyvo.veterinary.appointment.domain.model.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "veterinarian_id", nullable = false)
    private UUID veterinarianId;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AppointmentEntity() {
    }

    public AppointmentEntity(UUID id, UUID petId, UUID veterinarianId, UUID tutorId, LocalDateTime scheduledAt, AppointmentStatus status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.tutorId = tutorId;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public UUID getVeterinarianId() {
        return veterinarianId;
    }

    public void setVeterinarianId(UUID veterinarianId) {
        this.veterinarianId = veterinarianId;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(UUID tutorId) {
        this.tutorId = tutorId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID petId;
        private UUID veterinarianId;
        private UUID tutorId;
        private LocalDateTime scheduledAt;
        private AppointmentStatus status;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Builder() {}

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder petId(UUID petId) {
            this.petId = petId;
            return this;
        }

        public Builder veterinarianId(UUID veterinarianId) {
            this.veterinarianId = veterinarianId;
            return this;
        }

        public Builder tutorId(UUID tutorId) {
            this.tutorId = tutorId;
            return this;
        }

        public Builder scheduledAt(LocalDateTime scheduledAt) {
            this.scheduledAt = scheduledAt;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AppointmentEntity build() {
            return new AppointmentEntity(id, petId, veterinarianId, tutorId, scheduledAt, status, notes, createdAt, updatedAt);
        }
    }
}

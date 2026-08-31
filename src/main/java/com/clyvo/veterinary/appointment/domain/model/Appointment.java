package com.clyvo.veterinary.appointment.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private UUID id;
    private UUID petId;
    private UUID veterinarianId;
    private UUID tutorId;
    private LocalDateTime scheduledAt;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Appointment(UUID id, UUID petId, UUID veterinarianId, UUID tutorId, LocalDateTime scheduledAt, AppointmentStatus status, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    public static Appointment schedule(UUID petId, UUID veterinarianId, UUID tutorId, LocalDateTime scheduledAt, String notes) {
        return new Appointment(UUID.randomUUID(), petId, veterinarianId, tutorId, scheduledAt, AppointmentStatus.SCHEDULED, notes, LocalDateTime.now(), LocalDateTime.now());
    }

    public void confirm() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled appointments can be confirmed.");
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        if (this.status != AppointmentStatus.CONFIRMED && this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot start appointment in current status.");
        }
        this.status = AppointmentStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete(String finalNotes) {
        if (this.status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only in-progress appointments can be completed.");
        }
        this.status = AppointmentStatus.COMPLETED;
        this.notes = finalNotes;
        this.updatedAt = LocalDateTime.now();
    }

    public void reschedule(LocalDateTime newDate) {
        if (!isModifiable()) {
            throw new IllegalStateException("Cannot reschedule in current status.");
        }
        this.scheduledAt = newDate;
        this.status = AppointmentStatus.SCHEDULED; 
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isModifiable() {
        return this.status == AppointmentStatus.SCHEDULED || this.status == AppointmentStatus.CONFIRMED;
    }

    public UUID getId() { return id; }
    public UUID getPetId() { return petId; }
    public UUID getVeterinarianId() { return veterinarianId; }
    public UUID getTutorId() { return tutorId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

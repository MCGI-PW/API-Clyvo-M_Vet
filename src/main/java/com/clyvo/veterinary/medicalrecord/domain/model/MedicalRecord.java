package com.clyvo.veterinary.medicalrecord.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class MedicalRecord {
    private UUID id;
    private UUID appointmentId;
    private UUID petId;
    private UUID veterinarianId;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MedicalRecord(UUID id, UUID appointmentId, UUID petId, UUID veterinarianId, String symptoms, String diagnosis, String treatment, String observations, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MedicalRecord create(UUID appointmentId, UUID petId, UUID veterinarianId, String symptoms, String diagnosis, String treatment) {
        return new MedicalRecord(UUID.randomUUID(), appointmentId, petId, veterinarianId, symptoms, diagnosis, treatment, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public void addObservations(String obs) {
        if (this.observations == null) {
            this.observations = obs;
        } else {
            this.observations += "\n" + obs;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTreatment(String newTreatment) {
        this.treatment = newTreatment;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getAppointmentId() { return appointmentId; }
    public UUID getPetId() { return petId; }
    public UUID getVeterinarianId() { return veterinarianId; }
    public String getSymptoms() { return symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public String getObservations() { return observations; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

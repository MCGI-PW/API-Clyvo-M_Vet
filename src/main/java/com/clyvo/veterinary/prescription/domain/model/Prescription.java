package com.clyvo.veterinary.prescription.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Prescription {
    private final UUID id;
    private final UUID medicalRecordId;
    private final UUID petId;
    private final UUID veterinarianId;
    private final List<PrescriptionItem> medications;
    private final String generalInstructions;
    private final LocalDate validUntil;
    private final LocalDateTime createdAt;

    private Prescription(UUID id, UUID medicalRecordId, UUID petId, UUID veterinarianId,
                         List<PrescriptionItem> medications, String generalInstructions,
                         LocalDate validUntil, LocalDateTime createdAt) {
        this.id = id;
        this.medicalRecordId = medicalRecordId;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.medications = new ArrayList<>(medications);
        this.generalInstructions = generalInstructions;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

    public static Prescription create(UUID medicalRecordId, UUID petId, UUID veterinarianId,
                                      List<PrescriptionItem> medications, String generalInstructions,
                                      LocalDate validUntil) {
        return new Prescription(
                UUID.randomUUID(),
                medicalRecordId,
                petId,
                veterinarianId,
                medications,
                generalInstructions,
                validUntil,
                LocalDateTime.now()
        );
    }

    public static Prescription load(UUID id, UUID medicalRecordId, UUID petId, UUID veterinarianId,
                                      List<PrescriptionItem> medications, String generalInstructions,
                                      LocalDate validUntil, LocalDateTime createdAt) {
        return new Prescription(id, medicalRecordId, petId, veterinarianId, medications, generalInstructions, validUntil, createdAt);
    }

    public void addMedication(PrescriptionItem item) {
        this.medications.add(item);
    }

    public void removeMedication(String medicationName) {
        this.medications.removeIf(m -> m.medicationName().equals(medicationName));
    }

    public boolean isValid() {
        return validUntil == null || !validUntil.isBefore(LocalDate.now());
    }

    public UUID getId() { return id; }
    public UUID getMedicalRecordId() { return medicalRecordId; }
    public UUID getPetId() { return petId; }
    public UUID getVeterinarianId() { return veterinarianId; }
    public List<PrescriptionItem> getMedications() { return Collections.unmodifiableList(medications); }
    public String getGeneralInstructions() { return generalInstructions; }
    public LocalDate getValidUntil() { return validUntil; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

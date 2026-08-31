package com.clyvo.veterinary.vaccine.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Vaccine {
    private final UUID id;
    private final UUID petId;
    private final UUID veterinarianId;
    private final String vaccineName;
    private final String manufacturer;
    private final String batchNumber;
    private final LocalDate appliedAt;
    private final LocalDate nextDoseAt;
    private String notes;
    private final LocalDateTime createdAt;

    private Vaccine(UUID id, UUID petId, UUID veterinarianId, String vaccineName,
                    String manufacturer, String batchNumber, LocalDate appliedAt,
                    LocalDate nextDoseAt, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.petId = petId;
        this.veterinarianId = veterinarianId;
        this.vaccineName = vaccineName;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.appliedAt = appliedAt;
        this.nextDoseAt = nextDoseAt;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public static Vaccine register(UUID petId, UUID veterinarianId, String vaccineName,
                                   String manufacturer, String batchNumber,
                                   LocalDate appliedAt, LocalDate nextDoseAt) {
        return new Vaccine(
                UUID.randomUUID(),
                petId,
                veterinarianId,
                vaccineName,
                manufacturer,
                batchNumber,
                appliedAt,
                nextDoseAt,
                null,
                LocalDateTime.now()
        );
    }

    public static Vaccine load(UUID id, UUID petId, UUID veterinarianId, String vaccineName,
                               String manufacturer, String batchNumber, LocalDate appliedAt,
                               LocalDate nextDoseAt, String notes, LocalDateTime createdAt) {
        return new Vaccine(id, petId, veterinarianId, vaccineName, manufacturer, batchNumber, appliedAt, nextDoseAt, notes, createdAt);
    }

    public void addNotes(String notes) {
        this.notes = notes;
    }

    public boolean isNextDoseDue() {
        if (this.nextDoseAt == null) {
            return false;
        }
        return !LocalDate.now().plusDays(30).isBefore(this.nextDoseAt);
    }

    public UUID getId() { return id; }
    public UUID getPetId() { return petId; }
    public UUID getVeterinarianId() { return veterinarianId; }
    public String getVaccineName() { return vaccineName; }
    public String getManufacturer() { return manufacturer; }
    public String getBatchNumber() { return batchNumber; }
    public LocalDate getAppliedAt() { return appliedAt; }
    public LocalDate getNextDoseAt() { return nextDoseAt; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

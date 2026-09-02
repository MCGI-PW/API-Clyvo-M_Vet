package com.clyvo.veterinary.vaccine.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Vaccine {
    private final UUID id;
    private final UUID petId;
    private final String vaccineName;
    private final String manufacturer;
    private final String batchNumber;
    private final LocalDate appliedAt;
    private final LocalDate nextDoseAt;
    private final String notes;
    private final LocalDateTime createdAt;

    private Vaccine(UUID id, UUID petId, String vaccineName,
                    String manufacturer, String batchNumber, LocalDate appliedAt,
                    LocalDate nextDoseAt, String notes, LocalDateTime createdAt) {
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

    public static Vaccine register(UUID petId, String vaccineName,
                                   String manufacturer, String batchNumber, LocalDate appliedAt,
                                   LocalDate nextDoseAt, String notes) {
        return new Vaccine(
                UUID.randomUUID(),
                petId,
                vaccineName,
                manufacturer,
                batchNumber,
                appliedAt,
                nextDoseAt,
                notes,
                LocalDateTime.now()
        );
    }

    public static Vaccine load(UUID id, UUID petId, String vaccineName,
                               String manufacturer, String batchNumber, LocalDate appliedAt,
                               LocalDate nextDoseAt, String notes, LocalDateTime createdAt) {
        return new Vaccine(id, petId, vaccineName, manufacturer, batchNumber, appliedAt, nextDoseAt, notes, createdAt);
    }

    public UUID getId() { return id; }
    public UUID getPetId() { return petId; }
    public String getVaccineName() { return vaccineName; }
    public String getManufacturer() { return manufacturer; }
    public String getBatchNumber() { return batchNumber; }
    public LocalDate getAppliedAt() { return appliedAt; }
    public LocalDate getNextDoseAt() { return nextDoseAt; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isNextDoseDue() {
        if (nextDoseAt == null) {
            return false;
        }
        return !nextDoseAt.isAfter(LocalDate.now());
    }
}

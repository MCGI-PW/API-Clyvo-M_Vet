package com.clyvo.veterinary.prescription.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class PrescriptionItemEmbeddable {
    @Column(name = "medication_name", nullable = false)
    private String medicationName;
    
    private String dosage;
    private String frequency;
    private String duration;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;

    public PrescriptionItemEmbeddable() {
    }

    public PrescriptionItemEmbeddable(String medicationName, String dosage, String frequency, String duration, String instructions) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
    }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrescriptionItemEmbeddable that = (PrescriptionItemEmbeddable) o;
        return Objects.equals(medicationName, that.medicationName) &&
               Objects.equals(dosage, that.dosage) &&
               Objects.equals(frequency, that.frequency) &&
               Objects.equals(duration, that.duration) &&
               Objects.equals(instructions, that.instructions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicationName, dosage, frequency, duration, instructions);
    }

    @Override
    public String toString() {
        return "PrescriptionItemEmbeddable{" +
                "medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", duration='" + duration + '\'' +
                ", instructions='" + instructions + '\'' +
                '}';
    }
}

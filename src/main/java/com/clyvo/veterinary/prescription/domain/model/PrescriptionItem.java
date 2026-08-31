package com.clyvo.veterinary.prescription.domain.model;

public record PrescriptionItem(
    String medicationName,
    String dosage,
    String frequency,
    String duration,
    String instructions
) {
}

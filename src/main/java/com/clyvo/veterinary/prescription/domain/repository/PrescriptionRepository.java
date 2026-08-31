package com.clyvo.veterinary.prescription.domain.repository;

import com.clyvo.veterinary.prescription.domain.model.Prescription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrescriptionRepository {
    Prescription save(Prescription prescription);
    Optional<Prescription> findById(UUID id);
    List<Prescription> findByPetId(UUID petId);
    List<Prescription> findByVeterinarianId(UUID veterinarianId);
    Optional<Prescription> findByMedicalRecordId(UUID medicalRecordId);
    List<Prescription> findAll();
    void deleteById(UUID id);
}

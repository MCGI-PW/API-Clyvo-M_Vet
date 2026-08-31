package com.clyvo.veterinary.medicalrecord.domain.repository;

import com.clyvo.veterinary.medicalrecord.domain.model.MedicalRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository {
    MedicalRecord save(MedicalRecord mr);
    Optional<MedicalRecord> findById(UUID id);
    List<MedicalRecord> findByPetId(UUID petId);
    List<MedicalRecord> findByVeterinarianId(UUID veterinarianId);
    Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);
    List<MedicalRecord> findAll();
    void deleteById(UUID id);
}

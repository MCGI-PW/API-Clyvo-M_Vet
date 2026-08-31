package com.clyvo.veterinary.medicalrecord.infrastructure.persistence.repository;

import com.clyvo.veterinary.medicalrecord.infrastructure.persistence.entity.MedicalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecordEntity, UUID> {
    List<MedicalRecordEntity> findByPetId(UUID petId);
    List<MedicalRecordEntity> findByVeterinarianId(UUID veterinarianId);
    Optional<MedicalRecordEntity> findByAppointmentId(UUID appointmentId);
}

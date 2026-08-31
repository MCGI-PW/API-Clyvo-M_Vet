package com.clyvo.veterinary.prescription.infrastructure.persistence.repository;

import com.clyvo.veterinary.prescription.infrastructure.persistence.entity.PrescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionJpaRepository extends JpaRepository<PrescriptionEntity, UUID> {
    List<PrescriptionEntity> findByPetId(UUID petId);
    List<PrescriptionEntity> findByVeterinarianId(UUID veterinarianId);
    Optional<PrescriptionEntity> findByMedicalRecordId(UUID medicalRecordId);
}

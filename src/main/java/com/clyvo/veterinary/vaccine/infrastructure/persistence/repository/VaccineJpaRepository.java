package com.clyvo.veterinary.vaccine.infrastructure.persistence.repository;

import com.clyvo.veterinary.vaccine.infrastructure.persistence.entity.VaccineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VaccineJpaRepository extends JpaRepository<VaccineEntity, UUID> {
    List<VaccineEntity> findByPetId(UUID petId);
    List<VaccineEntity> findByVeterinarianId(UUID vetId);
    List<VaccineEntity> findByNextDoseAtBefore(LocalDate date);
}

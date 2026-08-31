package com.clyvo.veterinary.pet.infrastructure.persistence.repository;

import com.clyvo.veterinary.pet.infrastructure.persistence.entity.PetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PetJpaRepository extends JpaRepository<PetEntity, UUID> {
    List<PetEntity> findByTutorId(UUID tutorId);
    List<PetEntity> findByTutorIdAndActive(UUID tutorId, boolean active);
}

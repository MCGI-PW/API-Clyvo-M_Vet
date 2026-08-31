package com.clyvo.veterinary.veterinarian.infrastructure.persistence.repository;

import com.clyvo.veterinary.veterinarian.infrastructure.persistence.entity.VeterinarianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VeterinarianJpaRepository extends JpaRepository<VeterinarianEntity, UUID> {
    Optional<VeterinarianEntity> findByUserId(UUID userId);
    Optional<VeterinarianEntity> findByCrm(String crm);
    boolean existsByCrm(String crm);
}

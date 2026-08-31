package com.clyvo.veterinary.tutor.infrastructure.persistence.repository;

import com.clyvo.veterinary.tutor.infrastructure.persistence.entity.TutorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TutorJpaRepository extends JpaRepository<TutorEntity, UUID> {
    Optional<TutorEntity> findByUserId(UUID userId);
    Optional<TutorEntity> findByDocument(String document);
    boolean existsByDocument(String document);
}

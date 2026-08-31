package com.clyvo.veterinary.tutor.domain.repository;

import com.clyvo.veterinary.tutor.domain.model.Tutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TutorRepository {
    Tutor save(Tutor tutor);
    Optional<Tutor> findById(UUID id);
    Optional<Tutor> findByUserId(UUID userId);
    Optional<Tutor> findByDocument(String document);
    boolean existsByDocument(String document);
    List<Tutor> findAll();
    void deleteById(UUID id);
}

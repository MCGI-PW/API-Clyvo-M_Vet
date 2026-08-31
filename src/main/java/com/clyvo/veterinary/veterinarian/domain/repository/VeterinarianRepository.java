package com.clyvo.veterinary.veterinarian.domain.repository;

import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeterinarianRepository {
    Veterinarian save(Veterinarian vet);
    Optional<Veterinarian> findById(UUID id);
    Optional<Veterinarian> findByUserId(UUID userId);
    Optional<Veterinarian> findByCrm(String crm);
    boolean existsByCrm(String crm);
    List<Veterinarian> findAll();
    void deleteById(UUID id);
}

package com.clyvo.veterinary.vaccine.domain.repository;

import com.clyvo.veterinary.vaccine.domain.model.Vaccine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaccineRepository {
    Vaccine save(Vaccine vaccine);
    Optional<Vaccine> findById(UUID id);
    List<Vaccine> findByPetId(UUID petId);
    List<Vaccine> findByVeterinarianId(UUID veterinarianId);
    List<Vaccine> findAll();
    void deleteById(UUID id);
}

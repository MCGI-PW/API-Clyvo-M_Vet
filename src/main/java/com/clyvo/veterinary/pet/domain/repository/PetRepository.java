package com.clyvo.veterinary.pet.domain.repository;

import com.clyvo.veterinary.pet.domain.model.Pet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository {
    Pet save(Pet pet);
    Optional<Pet> findById(UUID id);
    List<Pet> findByTutorId(UUID tutorId);
    List<Pet> findAll();
    void deleteById(UUID id);
}

package com.clyvo.veterinary.pet.infrastructure.persistence.adapter;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.domain.repository.PetRepository;
import com.clyvo.veterinary.pet.infrastructure.persistence.mapper.PetEntityMapper;
import com.clyvo.veterinary.pet.infrastructure.persistence.repository.PetJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PetRepositoryAdapter implements PetRepository {

    private final PetJpaRepository petJpaRepository;
    private final PetEntityMapper mapper;

    public PetRepositoryAdapter(PetJpaRepository petJpaRepository, PetEntityMapper mapper) {
        this.petJpaRepository = petJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Pet save(Pet pet) {
        return mapper.toDomain(petJpaRepository.save(mapper.toEntity(pet)));
    }

    @Override
    public Optional<Pet> findById(UUID id) {
        return petJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Pet> findByTutorId(UUID tutorId) {
        return petJpaRepository.findByTutorId(tutorId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Pet> findAll() {
        return petJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        petJpaRepository.deleteById(id);
    }
}

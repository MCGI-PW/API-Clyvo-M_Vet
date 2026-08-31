package com.clyvo.veterinary.veterinarian.infrastructure.persistence.adapter;

import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import com.clyvo.veterinary.veterinarian.domain.repository.VeterinarianRepository;
import com.clyvo.veterinary.veterinarian.infrastructure.persistence.entity.VeterinarianEntity;
import com.clyvo.veterinary.veterinarian.infrastructure.persistence.mapper.VeterinarianEntityMapper;
import com.clyvo.veterinary.veterinarian.infrastructure.persistence.repository.VeterinarianJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class VeterinarianRepositoryAdapter implements VeterinarianRepository {

    private final VeterinarianJpaRepository jpaRepository;
    private final VeterinarianEntityMapper mapper;

    public VeterinarianRepositoryAdapter(VeterinarianJpaRepository jpaRepository, VeterinarianEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Veterinarian save(Veterinarian vet) {
        VeterinarianEntity entity = mapper.toEntity(vet);
        VeterinarianEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Veterinarian> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Veterinarian> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Veterinarian> findByCrm(String crm) {
        return jpaRepository.findByCrm(crm).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCrm(String crm) {
        return jpaRepository.existsByCrm(crm);
    }

    @Override
    public List<Veterinarian> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

package com.clyvo.veterinary.vaccine.infrastructure.persistence.adapter;

import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import com.clyvo.veterinary.vaccine.domain.repository.VaccineRepository;
import com.clyvo.veterinary.vaccine.infrastructure.persistence.entity.VaccineEntity;
import com.clyvo.veterinary.vaccine.infrastructure.persistence.mapper.VaccineEntityMapper;
import com.clyvo.veterinary.vaccine.infrastructure.persistence.repository.VaccineJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class VaccineRepositoryAdapter implements VaccineRepository {

    private final VaccineJpaRepository jpaRepository;
    private final VaccineEntityMapper mapper;

    public VaccineRepositoryAdapter(VaccineJpaRepository jpaRepository, VaccineEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Vaccine save(Vaccine vaccine) {
        VaccineEntity entity = mapper.toEntity(vaccine);
        VaccineEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Vaccine> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Vaccine> findByPetId(UUID petId) {
        return jpaRepository.findByPetId(petId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

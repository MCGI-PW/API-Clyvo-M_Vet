package com.clyvo.veterinary.prescription.infrastructure.persistence.adapter;

import com.clyvo.veterinary.prescription.domain.model.Prescription;
import com.clyvo.veterinary.prescription.domain.repository.PrescriptionRepository;
import com.clyvo.veterinary.prescription.infrastructure.persistence.mapper.PrescriptionEntityMapper;
import com.clyvo.veterinary.prescription.infrastructure.persistence.repository.PrescriptionJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PrescriptionRepositoryAdapter implements PrescriptionRepository {

    private final PrescriptionJpaRepository jpaRepository;
    private final PrescriptionEntityMapper mapper;

    public PrescriptionRepositoryAdapter(PrescriptionJpaRepository jpaRepository, PrescriptionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Prescription save(Prescription prescription) {
        var entity = mapper.toEntity(prescription);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Prescription> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Prescription> findByPetId(UUID petId) {
        return jpaRepository.findByPetId(petId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Prescription> findByVeterinarianId(UUID veterinarianId) {
        return jpaRepository.findByVeterinarianId(veterinarianId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Prescription> findByMedicalRecordId(UUID medicalRecordId) {
        return jpaRepository.findByMedicalRecordId(medicalRecordId).map(mapper::toDomain);
    }

    @Override
    public List<Prescription> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

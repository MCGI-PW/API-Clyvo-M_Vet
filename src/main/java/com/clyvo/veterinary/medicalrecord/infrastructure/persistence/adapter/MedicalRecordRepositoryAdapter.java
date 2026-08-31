package com.clyvo.veterinary.medicalrecord.infrastructure.persistence.adapter;

import com.clyvo.veterinary.medicalrecord.domain.model.MedicalRecord;
import com.clyvo.veterinary.medicalrecord.domain.repository.MedicalRecordRepository;
import com.clyvo.veterinary.medicalrecord.infrastructure.persistence.mapper.MedicalRecordEntityMapper;
import com.clyvo.veterinary.medicalrecord.infrastructure.persistence.repository.MedicalRecordJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class MedicalRecordRepositoryAdapter implements MedicalRecordRepository {

    private final MedicalRecordJpaRepository repository;
    private final MedicalRecordEntityMapper mapper;

    public MedicalRecordRepositoryAdapter(MedicalRecordJpaRepository repository, MedicalRecordEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MedicalRecord save(MedicalRecord mr) {
        return mapper.toDomain(repository.save(mapper.toEntity(mr)));
    }

    @Override
    public Optional<MedicalRecord> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MedicalRecord> findByPetId(UUID petId) {
        return repository.findByPetId(petId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecord> findByVeterinarianId(UUID veterinarianId) {
        return repository.findByVeterinarianId(veterinarianId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<MedicalRecord> findByAppointmentId(UUID appointmentId) {
        return repository.findByAppointmentId(appointmentId).map(mapper::toDomain);
    }

    @Override
    public List<MedicalRecord> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}

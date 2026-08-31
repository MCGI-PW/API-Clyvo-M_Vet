package com.clyvo.veterinary.appointment.infrastructure.persistence.adapter;

import com.clyvo.veterinary.appointment.domain.model.Appointment;
import com.clyvo.veterinary.appointment.domain.repository.AppointmentRepository;
import com.clyvo.veterinary.appointment.infrastructure.persistence.mapper.AppointmentEntityMapper;
import com.clyvo.veterinary.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository repository;
    private final AppointmentEntityMapper mapper;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository repository, AppointmentEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Appointment save(Appointment a) {
        return mapper.toDomain(repository.save(mapper.toEntity(a)));
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findByVeterinarianId(UUID vetId) {
        return repository.findByVeterinarianId(vetId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByTutorId(UUID tutorId) {
        return repository.findByTutorId(tutorId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByPetId(UUID petId) {
        return repository.findByPetId(petId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByVeterinarianIdAndScheduledAtBetween(UUID vetId, LocalDateTime start, LocalDateTime end) {
        return repository.findByVeterinarianIdAndScheduledAtBetween(vetId, start, end).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}

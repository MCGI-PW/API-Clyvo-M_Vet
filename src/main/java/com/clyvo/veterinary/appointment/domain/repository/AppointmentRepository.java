package com.clyvo.veterinary.appointment.domain.repository;

import com.clyvo.veterinary.appointment.domain.model.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository {
    Appointment save(Appointment a);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByVeterinarianId(UUID vetId);
    List<Appointment> findByTutorId(UUID tutorId);
    List<Appointment> findByPetId(UUID petId);
    List<Appointment> findByVeterinarianIdAndScheduledAtBetween(UUID vetId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findAll();
    void deleteById(UUID id);
}

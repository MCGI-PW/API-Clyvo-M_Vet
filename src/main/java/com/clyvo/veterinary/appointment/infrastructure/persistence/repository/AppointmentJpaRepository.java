package com.clyvo.veterinary.appointment.infrastructure.persistence.repository;

import com.clyvo.veterinary.appointment.domain.model.AppointmentStatus;
import com.clyvo.veterinary.appointment.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {
    List<AppointmentEntity> findByVeterinarianId(UUID veterinarianId);
    List<AppointmentEntity> findByTutorId(UUID tutorId);
    List<AppointmentEntity> findByPetId(UUID petId);
    List<AppointmentEntity> findByVeterinarianIdAndScheduledAtBetween(UUID veterinarianId, LocalDateTime start, LocalDateTime end);
    List<AppointmentEntity> findByVeterinarianIdAndStatus(UUID veterinarianId, AppointmentStatus status);
}

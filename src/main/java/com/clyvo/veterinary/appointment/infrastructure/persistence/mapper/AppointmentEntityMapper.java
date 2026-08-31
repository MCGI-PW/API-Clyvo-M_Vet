package com.clyvo.veterinary.appointment.infrastructure.persistence.mapper;

import com.clyvo.veterinary.appointment.domain.model.Appointment;
import com.clyvo.veterinary.appointment.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEntityMapper {

    public AppointmentEntity toEntity(Appointment a) {
        if (a == null) return null;
        return AppointmentEntity.builder()
                .id(a.getId())
                .petId(a.getPetId())
                .veterinarianId(a.getVeterinarianId())
                .tutorId(a.getTutorId())
                .scheduledAt(a.getScheduledAt())
                .status(a.getStatus())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public Appointment toDomain(AppointmentEntity entity) {
        if (entity == null) return null;
        try {
            java.lang.reflect.Constructor<Appointment> constructor = Appointment.class.getDeclaredConstructor(
                    java.util.UUID.class, java.util.UUID.class, java.util.UUID.class, java.util.UUID.class,
                    java.time.LocalDateTime.class, com.clyvo.veterinary.appointment.domain.model.AppointmentStatus.class,
                    String.class, java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(), entity.getPetId(), entity.getVeterinarianId(), entity.getTutorId(),
                    entity.getScheduledAt(), entity.getStatus(), entity.getNotes(), entity.getCreatedAt(), entity.getUpdatedAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AppointmentEntity to Domain", e);
        }
    }
}

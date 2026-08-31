package com.clyvo.veterinary.appointment.application.mapper;

import com.clyvo.veterinary.appointment.application.dto.AppointmentResponse;
import com.clyvo.veterinary.appointment.domain.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentDtoMapper {

    public AppointmentResponse toResponse(Appointment a, Object pet, Object vet, Object vetUser, Object tutorUser) {
        return new AppointmentResponse(
                a.getId(),
                a.getPetId(),
                "Pet Name",
                a.getVeterinarianId(),
                "Vet Name",
                "CRM-123",
                a.getTutorId(),
                "Tutor Name",
                a.getScheduledAt(),
                a.getStatus().getDisplayName(),
                a.getNotes(),
                a.getCreatedAt()
        );
    }
}

package com.clyvo.veterinary.appointment.application.service;

import com.clyvo.veterinary.appointment.application.dto.AppointmentResponse;
import com.clyvo.veterinary.appointment.application.dto.ScheduleAppointmentRequest;
import com.clyvo.veterinary.appointment.application.mapper.AppointmentDtoMapper;
import com.clyvo.veterinary.appointment.application.port.in.AppointmentUseCase;
import com.clyvo.veterinary.appointment.domain.model.Appointment;
import com.clyvo.veterinary.appointment.domain.repository.AppointmentRepository;
import com.clyvo.veterinary.appointment.domain.service.AppointmentDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService implements AppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentDomainService appointmentDomainService;
    private final AppointmentDtoMapper dtoMapper;
    
    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentDomainService appointmentDomainService, AppointmentDtoMapper dtoMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentDomainService = appointmentDomainService;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public AppointmentResponse scheduleAppointment(UUID tutorId, ScheduleAppointmentRequest request) {
        appointmentDomainService.validateSchedulingRules(request.scheduledAt());
        List<Appointment> existing = appointmentRepository.findByVeterinarianId(request.veterinarianId());
        if (appointmentDomainService.hasConflict(existing, request.scheduledAt())) {
            throw new IllegalArgumentException("Veterinarian has a conflicting appointment.");
        }

        Appointment appointment = Appointment.schedule(request.petId(), request.veterinarianId(), tutorId, request.scheduledAt(), request.notes());
        Appointment saved = appointmentRepository.save(appointment);
        return dtoMapper.toResponse(saved, null, null, null, null);
    }

    @Override
    public AppointmentResponse confirmAppointment(UUID id) {
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        a.confirm();
        return dtoMapper.toResponse(appointmentRepository.save(a), null, null, null, null);
    }

    @Override
    public AppointmentResponse cancelAppointment(UUID id) {
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        a.cancel();
        return dtoMapper.toResponse(appointmentRepository.save(a), null, null, null, null);
    }

    @Override
    public AppointmentResponse completeAppointment(UUID id, String finalNotes) {
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        a.complete(finalNotes);
        return dtoMapper.toResponse(appointmentRepository.save(a), null, null, null, null);
    }

    @Override
    public AppointmentResponse getAppointment(UUID id) {
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        return dtoMapper.toResponse(a, null, null, null, null);
    }

    @Override
    public List<AppointmentResponse> listByVeterinarian(UUID vetId) {
        return appointmentRepository.findByVeterinarianId(vetId).stream()
                .map(a -> dtoMapper.toResponse(a, null, null, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> listByTutor(UUID tutorId) {
        return appointmentRepository.findByTutorId(tutorId).stream()
                .map(a -> dtoMapper.toResponse(a, null, null, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> listByPet(UUID petId) {
        return appointmentRepository.findByPetId(petId).stream()
                .map(a -> dtoMapper.toResponse(a, null, null, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> listAll() {
        return appointmentRepository.findAll().stream()
                .map(a -> dtoMapper.toResponse(a, null, null, null, null))
                .collect(Collectors.toList());
    }
}

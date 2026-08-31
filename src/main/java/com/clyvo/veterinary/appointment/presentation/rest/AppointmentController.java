package com.clyvo.veterinary.appointment.presentation.rest;

import com.clyvo.veterinary.appointment.application.dto.AppointmentResponse;
import com.clyvo.veterinary.appointment.application.dto.ScheduleAppointmentRequest;
import com.clyvo.veterinary.appointment.application.port.in.AppointmentUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentUseCase appointmentUseCase;

    public AppointmentController(AppointmentUseCase appointmentUseCase) {
        this.appointmentUseCase = appointmentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public AppointmentResponse scheduleAppointment(@RequestBody @Valid ScheduleAppointmentRequest request) {
        UUID tutorId = UUID.randomUUID(); 
        return appointmentUseCase.scheduleAppointment(tutorId, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse getAppointment(@PathVariable UUID id) {
        return appointmentUseCase.getAppointment(id);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'ADMIN')")
    public AppointmentResponse confirmAppointment(@PathVariable UUID id) {
        return appointmentUseCase.confirmAppointment(id);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public AppointmentResponse cancelAppointment(@PathVariable UUID id) {
        return appointmentUseCase.cancelAppointment(id);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('VETERINARIAN', 'ADMIN')")
    public AppointmentResponse completeAppointment(@PathVariable UUID id, @RequestParam String finalNotes) {
        return appointmentUseCase.completeAppointment(id, finalNotes);
    }

    @GetMapping("/vet/me")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public List<AppointmentResponse> listVetAppointments() {
        UUID vetId = UUID.randomUUID();
        return appointmentUseCase.listByVeterinarian(vetId);
    }

    @GetMapping("/tutor/me")
    @PreAuthorize("hasRole('TUTOR')")
    public List<AppointmentResponse> listTutorAppointments() {
        UUID tutorId = UUID.randomUUID();
        return appointmentUseCase.listByTutor(tutorId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppointmentResponse> listAll() {
        return appointmentUseCase.listAll();
    }
}

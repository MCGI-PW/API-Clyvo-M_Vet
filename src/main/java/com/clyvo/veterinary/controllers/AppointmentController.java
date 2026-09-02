package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.CompleteAppointmentRequest;
import com.clyvo.veterinary.dto.ScheduleAppointmentRequest;
import com.clyvo.veterinary.models.Appointment;
import com.clyvo.veterinary.services.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentService appointmentService) { this.appointmentService = appointmentService; }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<Appointment> schedule(@RequestBody ScheduleAppointmentRequest request) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(appointmentService.scheduleAppointment(UUID.fromString(userIdStr), request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('VETERINARIAN')")
    public ResponseEntity<Appointment> complete(@PathVariable UUID id, @RequestBody CompleteAppointmentRequest request) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(appointmentService.completeAppointment(UUID.fromString(userIdStr), id, request));
    }
}

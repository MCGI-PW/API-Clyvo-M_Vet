package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.CompleteAppointmentRequest;
import com.clyvo.veterinary.services.ConsultaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final ConsultaService consultaService;

    public AppointmentController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    private UUID getLoggedAccountId() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(idContaStr);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteAppointmentRequest request) {
        
        String notes = (request != null) ? request.getClinicalNotes() : null;
        consultaService.completeConsulta(id, getLoggedAccountId(), notes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable UUID id) {
        consultaService.cancelarConsulta(id, getLoggedAccountId());
        return ResponseEntity.ok().build();
    }
}

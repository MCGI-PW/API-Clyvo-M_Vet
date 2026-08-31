package com.clyvo.veterinary.appointment.presentation.web;

import com.clyvo.veterinary.appointment.application.dto.ScheduleAppointmentRequest;
import com.clyvo.veterinary.appointment.application.port.in.AppointmentUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class AppointmentWebController {

    private final AppointmentUseCase appointmentUseCase;

    public AppointmentWebController(AppointmentUseCase appointmentUseCase) {
        this.appointmentUseCase = appointmentUseCase;
    }

    @GetMapping("/tutor/appointments")
    public String listTutorAppointments(Model model) {
        UUID tutorId = UUID.randomUUID(); 
        model.addAttribute("appointments", appointmentUseCase.listByTutor(tutorId));
        return "tutor/appointments-list";
    }

    @GetMapping("/tutor/appointments/new")
    public String showSchedulingForm(Model model) {
        model.addAttribute("appointment", new ScheduleAppointmentRequest(null, null, null, null));
        return "tutor/appointment-form";
    }

    @PostMapping("/tutor/appointments/new")
    public String submitScheduling(@ModelAttribute ScheduleAppointmentRequest request) {
        UUID tutorId = UUID.randomUUID(); 
        appointmentUseCase.scheduleAppointment(tutorId, request);
        return "redirect:/tutor/appointments";
    }

    @GetMapping("/vet/appointments")
    public String listVetAppointments(Model model) {
        UUID vetId = UUID.randomUUID(); 
        model.addAttribute("appointments", appointmentUseCase.listByVeterinarian(vetId));
        return "vet/appointments-list";
    }

    @PostMapping("/vet/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable UUID id) {
        appointmentUseCase.confirmAppointment(id);
        return "redirect:/vet/appointments";
    }

    @PostMapping("/vet/appointments/{id}/complete")
    public String completeAppointment(@PathVariable UUID id, @RequestParam String notes) {
        appointmentUseCase.completeAppointment(id, notes);
        return "redirect:/vet/appointments";
    }
}

package com.clyvo.veterinary.medicalrecord.presentation.web;

import com.clyvo.veterinary.medicalrecord.application.dto.CreateMedicalRecordRequest;
import com.clyvo.veterinary.medicalrecord.application.port.in.MedicalRecordUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class MedicalRecordWebController {

    private final MedicalRecordUseCase useCase;

    public MedicalRecordWebController(MedicalRecordUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/vet/medical-records/new")
    public String showCreateForm(@RequestParam UUID appointmentId, @RequestParam UUID petId, Model model) {
        model.addAttribute("record", new CreateMedicalRecordRequest(appointmentId, petId, "", "", "", ""));
        return "vet/medical-record-form";
    }

    @PostMapping("/vet/medical-records/new")
    public String createRecord(@ModelAttribute CreateMedicalRecordRequest request) {
        UUID vetId = UUID.randomUUID(); 
        useCase.createRecord(vetId, request);
        return "redirect:/vet/appointments";
    }
    
    @GetMapping("/vet/medical-records/{id}")
    public String viewRecord(@PathVariable UUID id, Model model) {
        model.addAttribute("record", useCase.getRecord(id));
        return "vet/medical-record-view";
    }
}

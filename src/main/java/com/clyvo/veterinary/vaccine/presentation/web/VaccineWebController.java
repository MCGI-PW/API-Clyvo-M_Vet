package com.clyvo.veterinary.vaccine.presentation.web;

import com.clyvo.veterinary.shared.application.security.CurrentUser;
import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.application.port.in.VaccineUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class VaccineWebController {

    private final VaccineUseCase vaccineUseCase;

    public VaccineWebController(VaccineUseCase vaccineUseCase) {
        this.vaccineUseCase = vaccineUseCase;
    }

    @GetMapping("/vet/vaccines")
    public String myVaccines(@CurrentUser UserDetails currentUser, Model model) {
        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        List<VaccineResponse> vaccines = vaccineUseCase.listByVeterinarian(veterinarianId);
        model.addAttribute("vaccines", vaccines);
        model.addAttribute("pageTitle", "Minhas Vacinas Registradas");
        return "vet/vaccines";
    }

    @GetMapping("/vet/vaccines/new")
    public String newVaccineForm(Model model) {
        model.addAttribute("createVaccineRequest", new CreateVaccineRequest(
                null, "", "", "", null, null, ""
        ));
        model.addAttribute("pageTitle", "Registrar Vacina");
        return "vet/vaccine-new";
    }

    @PostMapping("/vet/vaccines/new")
    public String registerVaccine(
            @CurrentUser UserDetails currentUser,
            @Valid @ModelAttribute("createVaccineRequest") CreateVaccineRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Registrar Vacina");
            return "vet/vaccine-new";
        }

        UUID veterinarianId = UUID.fromString(currentUser.getUsername());
        vaccineUseCase.registerVaccine(veterinarianId, request);
        return "redirect:/vet/vaccines?registered";
    }

    @GetMapping("/tutor/pets/{petId}/vaccines")
    public String petVaccineHistory(@PathVariable UUID petId, Model model) {
        List<VaccineResponse> vaccines = vaccineUseCase.listByPet(petId);
        model.addAttribute("vaccines", vaccines);
        model.addAttribute("petId", petId);
        model.addAttribute("pageTitle", "Histórico de Vacinas");
        return "tutor/pet-vaccines";
    }
}

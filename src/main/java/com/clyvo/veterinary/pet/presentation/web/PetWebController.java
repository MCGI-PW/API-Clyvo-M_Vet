package com.clyvo.veterinary.pet.presentation.web;

import com.clyvo.veterinary.pet.application.dto.CreatePetRequest;
import com.clyvo.veterinary.pet.application.dto.PetResponse;
import com.clyvo.veterinary.pet.application.dto.UpdatePetRequest;
import com.clyvo.veterinary.pet.application.port.in.PetUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
public class PetWebController {

    private final PetUseCase petUseCase;

    public PetWebController(PetUseCase petUseCase) {
        this.petUseCase = petUseCase;
    }

    @GetMapping("/tutor/pets")
    public String listTutorPets(Model model) {
        UUID tutorId = UUID.randomUUID(); 
        model.addAttribute("pets", petUseCase.listPetsByTutor(tutorId));
        return "tutor/pets-list";
    }

    @GetMapping("/tutor/pets/new")
    public String showNewPetForm(Model model) {
        model.addAttribute("pet", new CreatePetRequest(null, null, null, null, null, null, null, null));
        return "tutor/pet-form";
    }

    @PostMapping("/tutor/pets/new")
    public String createPet(@ModelAttribute CreatePetRequest request) {
        petUseCase.createPet(request);
        return "redirect:/tutor/pets";
    }

    @GetMapping("/tutor/pets/{id}/edit")
    public String showEditPetForm(@PathVariable UUID id, Model model) {
        PetResponse pet = petUseCase.getPet(id);
        model.addAttribute("pet", pet);
        return "tutor/pet-edit";
    }

    @PostMapping("/tutor/pets/{id}/edit")
    public String editPet(@PathVariable UUID id, @ModelAttribute UpdatePetRequest request) {
        petUseCase.updatePet(id, request);
        return "redirect:/tutor/pets";
    }
}

package com.clyvo.veterinary.veterinarian.presentation.web;

import com.clyvo.veterinary.veterinarian.application.dto.UpdateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import com.clyvo.veterinary.veterinarian.application.port.in.VeterinarianUseCase;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/vet")
public class VeterinarianWebController {

    private final VeterinarianUseCase veterinarianUseCase;

    public VeterinarianWebController(VeterinarianUseCase veterinarianUseCase) {
        this.veterinarianUseCase = veterinarianUseCase;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "vet/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        UUID userId = extractUserId(authentication);
        VeterinarianResponse profile = veterinarianUseCase.getProfileByUserId(userId);
        model.addAttribute("profile", profile);
        return "vet/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Authentication authentication, Model model) {
        UUID userId = extractUserId(authentication);
        VeterinarianResponse profile = veterinarianUseCase.getProfileByUserId(userId);
        
        UpdateVeterinarianRequest updateReq = new UpdateVeterinarianRequest(
            null, 
            profile.bio(),
            profile.phone(),
            profile.profilePictureUrl()
        );
        model.addAttribute("request", updateReq);
        model.addAttribute("profile", profile);
        return "vet/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String saveProfile(Authentication authentication, 
                              @Valid @ModelAttribute("request") UpdateVeterinarianRequest request, 
                              BindingResult bindingResult, Model model) {
        UUID userId = extractUserId(authentication);
        VeterinarianResponse profile = veterinarianUseCase.getProfileByUserId(userId);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            return "vet/profile-edit";
        }
        
        veterinarianUseCase.updateProfile(profile.id(), request);
        return "redirect:/vet/profile";
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "redirect:/vet/appointments-list";
    }

    private UUID extractUserId(Authentication authentication) {
        return UUID.randomUUID(); 
    }
}

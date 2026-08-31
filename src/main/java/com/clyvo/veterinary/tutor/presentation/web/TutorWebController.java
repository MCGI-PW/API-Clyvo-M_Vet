package com.clyvo.veterinary.tutor.presentation.web;

import com.clyvo.veterinary.tutor.application.dto.TutorResponse;
import com.clyvo.veterinary.tutor.application.dto.UpdateTutorRequest;
import com.clyvo.veterinary.tutor.application.port.in.TutorUseCase;
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
@RequestMapping("/tutor")
public class TutorWebController {

    private final TutorUseCase tutorUseCase;

    public TutorWebController(TutorUseCase tutorUseCase) {
        this.tutorUseCase = tutorUseCase;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "tutor/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        UUID userId = extractUserId(authentication);
        TutorResponse profile = tutorUseCase.getProfileByUserId(userId);
        model.addAttribute("profile", profile);
        return "tutor/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Authentication authentication, Model model) {
        UUID userId = extractUserId(authentication);
        TutorResponse profile = tutorUseCase.getProfileByUserId(userId);
        
        UpdateTutorRequest updateReq = new UpdateTutorRequest(
            profile.phone(),
            profile.address(),
            profile.profilePictureUrl()
        );
        model.addAttribute("request", updateReq);
        model.addAttribute("profile", profile);
        return "tutor/profile-edit";
    }

    @PostMapping("/profile/edit")
    public String saveProfile(Authentication authentication, 
                              @Valid @ModelAttribute("request") UpdateTutorRequest request, 
                              BindingResult bindingResult, Model model) {
        UUID userId = extractUserId(authentication);
        TutorResponse profile = tutorUseCase.getProfileByUserId(userId);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("profile", profile);
            return "tutor/profile-edit";
        }
        
        tutorUseCase.updateProfile(profile.id(), request);
        return "redirect:/tutor/profile";
    }

    private UUID extractUserId(Authentication authentication) {
        return UUID.randomUUID(); 
    }
}

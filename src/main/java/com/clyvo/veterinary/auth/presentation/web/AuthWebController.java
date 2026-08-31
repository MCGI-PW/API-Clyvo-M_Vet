package com.clyvo.veterinary.auth.presentation.web;

import com.clyvo.veterinary.auth.application.dto.RegisterRequest;
import com.clyvo.veterinary.auth.application.port.in.RegisterUseCase;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthWebController {

    private final RegisterUseCase registerUseCase;

    public AuthWebController(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest(null, null, null, null));
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                                  BindingResult result,
                                  Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        try {
            registerUseCase.register(request);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}

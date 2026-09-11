package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.services.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebAuthController {

    private final AuthService authService;

    public WebAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if ("TUTOR".equals(role)) return "redirect:/web/tutor/dashboard";
        if ("VETERINARIO".equals(role)) return "redirect:/web/vet/dashboard";
        if ("CLINICA".equals(role)) return "redirect:/web/clinica/dashboard";
        return "redirect:/web/login";
    }

    @GetMapping("/web/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered) {
        model.addAttribute("loginRequest", new AuthRequest());
        model.addAttribute("registerRequest", new RegisterRequest());
        if (logout != null) {
            model.addAttribute("successMsg", "Sessão encerrada com sucesso.");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "Conta criada com sucesso! Faça login abaixo.");
        }
        return "web/login";
    }

    @PostMapping("/web/login")
    public String processLogin(@ModelAttribute("loginRequest") AuthRequest loginRequest,
                               HttpSession session,
                               Model model) {
        try {
            AuthResponse response = authService.login(loginRequest);
            session.setAttribute("userRole", response.getRole());
            session.setAttribute("jwtToken", response.getToken());
            session.setAttribute("userEmail", loginRequest.getEmail());

            if ("TUTOR".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/tutor/dashboard";
            } else if ("VETERINARIO".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/vet/dashboard";
            } else if ("CLINICA".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/clinica/dashboard";
            }
            return "redirect:/web/login";
        } catch (Exception e) {
            model.addAttribute("errorMsg", "Falha no login: " + e.getMessage());
            model.addAttribute("registerRequest", new RegisterRequest());
            return "web/login";
        }
    }

    @PostMapping("/web/register")
    public String processRegister(@ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                  HttpSession session,
                                  Model model) {
        try {
            AuthResponse response = authService.register(registerRequest);
            session.setAttribute("userRole", response.getRole());
            session.setAttribute("jwtToken", response.getToken());
            session.setAttribute("userEmail", registerRequest.getEmail());

            if ("TUTOR".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/tutor/dashboard";
            } else if ("VETERINARIO".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/vet/dashboard";
            } else if ("CLINICA".equalsIgnoreCase(response.getRole())) {
                return "redirect:/web/clinica/dashboard";
            }
            return "redirect:/web/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("registerErrorMsg", "Falha no cadastro: " + e.getMessage());
            model.addAttribute("loginRequest", new AuthRequest());
            return "web/login";
        }
    }

    @GetMapping("/web/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/web/login?logout=true";
    }
}

package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.services.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebAuthController {

    private final AuthService authService;
    private final boolean oauthMockEnabled;
    private final boolean oauthGoogleEnabled;
    private final boolean oauthGithubEnabled;

    public WebAuthController(AuthService authService,
                             @Value("${app.oauth2.mock-enabled:false}") boolean oauthMockEnabled,
                             @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
                             @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId) {
        this.authService = authService;
        this.oauthMockEnabled = oauthMockEnabled;
        this.oauthGoogleEnabled = isRealClientId(googleClientId);
        this.oauthGithubEnabled = isRealClientId(githubClientId);
    }

    private static boolean isRealClientId(String clientId) {
        return clientId != null && !clientId.isBlank() && !clientId.endsWith("-demo");
    }

    @ModelAttribute
    public void addOAuthFlags(Model model) {
        model.addAttribute("oauthMockEnabled", oauthMockEnabled);
        model.addAttribute("oauthGoogleEnabled", oauthGoogleEnabled);
        model.addAttribute("oauthGithubEnabled", oauthGithubEnabled);
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
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String error) {
        model.addAttribute("loginRequest", new AuthRequest());
        model.addAttribute("registerRequest", new RegisterRequest());
        if (logout != null) {
            model.addAttribute("successMsg", "Sessão encerrada com sucesso.");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "Conta criada com sucesso! Faça login abaixo.");
        }
        if ("oauth2".equals(error)) {
            model.addAttribute("errorMsg", "Falha na autenticação via OAuth2. Tente novamente.");
        }
        return "web/login";
    }

    @GetMapping("/web/login/oauth2-mock")
    public String mockOAuth2Login(@RequestParam(defaultValue = "google") String provider,
                                  HttpSession session) {
        // Simulação disponível apenas em ambiente local (app.oauth2.mock-enabled=true)
        if (!oauthMockEnabled) return "redirect:/web/login";

        String email = "google".equalsIgnoreCase(provider) ? "tutor.google@gmail.com" : "tutor.github@github.com";
        String name = "google".equalsIgnoreCase(provider) ? "Tutor Google Social" : "Tutor GitHub Social";

        AuthResponse response = authService.loginOrRegisterOAuth2(email, name);
        session.setAttribute("userRole", response.getRole());
        session.setAttribute("jwtToken", response.getToken());
        session.setAttribute("userEmail", email);
        session.setAttribute("userName", name);

        return "redirect:/web/tutor/dashboard";
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

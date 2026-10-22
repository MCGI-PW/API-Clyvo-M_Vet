package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.WebSession;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.exceptions.DomainException;
import com.clyvo.veterinary.services.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebAuthController {

    private static final String LOGIN_VIEW = "web/login";

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
        return "redirect:" + WebSession.dashboardPath((String) session.getAttribute(WebSession.ROLE));
    }

    @GetMapping("/web/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String denied,
                            @RequestParam(required = false) String unauthenticated) {
        prepareForms(model);
        if (logout != null) model.addAttribute(WebFeedback.SUCCESS, "msg.logout");
        if ("oauth2".equals(error)) model.addAttribute("errorKey", "msg.error.oauth2");
        if (denied != null) model.addAttribute("errorKey", "msg.error.denied");
        if (unauthenticated != null) model.addAttribute("errorKey", "msg.error.unauthenticated");
        return LOGIN_VIEW;
    }

    @GetMapping("/web/login/oauth2-mock")
    public String mockOAuth2Login(@RequestParam(defaultValue = "google") String provider, HttpSession session) {
        // Simulação disponível apenas em ambiente local (app.oauth2.mock-enabled=true)
        if (!oauthMockEnabled) return "redirect:/web/login";

        boolean google = "google".equalsIgnoreCase(provider);
        String email = google ? "tutor.google@gmail.com" : "tutor.github@github.com";
        String name = google ? "Tutor Google Social" : "Tutor GitHub Social";

        AuthResponse response = authService.loginOrRegisterOAuth2(email, name);
        return "redirect:" + WebSession.start(session, response, email, name);
    }

    @PostMapping("/web/login")
    public String processLogin(@ModelAttribute("loginRequest") AuthRequest loginRequest,
                               HttpSession session,
                               Model model) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return "redirect:" + WebSession.start(session, response, loginRequest.getEmail(), null);
        } catch (BadCredentialsException | DomainException e) {
            model.addAttribute(WebFeedback.ERROR, e.getMessage());
            model.addAttribute("registerRequest", new RegisterRequest());
            return LOGIN_VIEW;
        }
    }

    @PostMapping("/web/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                  BindingResult validation,
                                  HttpSession session,
                                  Model model) {
        if (validation.hasErrors()) {
            return registerError(model, validation.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            AuthResponse response = authService.register(registerRequest);
            return "redirect:" + WebSession.start(session, response, registerRequest.getEmail(), registerRequest.getName());
        } catch (DomainException e) {
            return registerError(model, e.getMessage());
        }
    }

    @GetMapping("/web/logout")
    public String logout(HttpSession session) {
        authService.logout((String) session.getAttribute(WebSession.TOKEN));
        session.invalidate();
        return "redirect:/web/login?logout=true";
    }

    private String registerError(Model model, String message) {
        model.addAttribute("registerErrorMsg", message);
        model.addAttribute("loginRequest", new AuthRequest());
        return LOGIN_VIEW;
    }

    private static void prepareForms(Model model) {
        model.addAttribute("loginRequest", new AuthRequest());
        model.addAttribute("registerRequest", new RegisterRequest());
    }
}

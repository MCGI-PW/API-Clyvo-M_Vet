package com.clyvo.veterinary.config;

import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public CustomOAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // 1. Extração segura dos atributos fornecidos pelo provedor (Google e GitHub)
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (name == null || name.isBlank()) {
            name = oauthUser.getAttribute("login");
            if (name == null || name.isBlank()) {
                name = "Usuário Social OAuth2";
            }
        }

        if (email == null || email.isBlank()) {
            String login = oauthUser.getAttribute("login");
            email = (login != null)
                    ? login.trim().toLowerCase() + "@github.user"
                    : "oauth2-" + UUID.randomUUID() + "@clyvovet.com";
        }
        email = email.trim().toLowerCase();

        // 2. Autenticação e auto-provisionamento centralizado via AuthService
        AuthResponse authResponse = authService.loginOrRegisterOAuth2(email, name);

        // 3. Associação de atributos à sessão HTTP para autorização do Thymeleaf
        HttpSession session = request.getSession(true);
        session.setAttribute("userRole", authResponse.getRole());
        session.setAttribute("jwtToken", authResponse.getToken());
        session.setAttribute("userEmail", email);
        session.setAttribute("userName", name);

        // 4. Redirecionamento baseado no perfil
        if ("VETERINARIO".equalsIgnoreCase(authResponse.getRole())) {
            response.sendRedirect("/web/vet/dashboard");
        } else if ("CLINICA".equalsIgnoreCase(authResponse.getRole())) {
            response.sendRedirect("/web/clinica/dashboard");
        } else {
            response.sendRedirect("/web/tutor/dashboard");
        }
    }
}

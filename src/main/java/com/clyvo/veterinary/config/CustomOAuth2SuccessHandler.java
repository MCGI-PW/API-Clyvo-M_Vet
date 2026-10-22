package com.clyvo.veterinary.config;

import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_NAME = "Usuário Social OAuth2";

    private final AuthService authService;

    public CustomOAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String login = oauthUser.getAttribute("login");
        String name = firstNonBlank(oauthUser.getAttribute("name"), login, DEFAULT_NAME);
        String email = resolveEmail(oauthUser.getAttribute("email"), login);

        // Autenticação e auto-provisionamento centralizado via AuthService
        AuthResponse authResponse = authService.loginOrRegisterOAuth2(email, name);
        response.sendRedirect(WebSession.start(request.getSession(true), authResponse, email, name));
    }

    /** O GitHub pode não expor o e-mail; nesse caso derivamos um identificador estável a partir do login. */
    private static String resolveEmail(String email, String login) {
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase();
        }
        if (login != null && !login.isBlank()) {
            return login.trim().toLowerCase() + "@github.user";
        }
        return "oauth2-" + UUID.randomUUID() + "@clyvovet.com";
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}

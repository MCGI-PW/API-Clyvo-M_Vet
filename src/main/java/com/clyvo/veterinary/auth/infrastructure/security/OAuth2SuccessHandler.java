package com.clyvo.veterinary.auth.infrastructure.security;

import com.clyvo.veterinary.user.domain.model.Role;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OAuth2SuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            user = User.createWithGoogle(name, email, googleId, Role.ROLE_TUTOR);
            userRepository.save(user);
        } else if (user.getGoogleId() == null) {
            user.updateGoogleId(googleId);
            userRepository.save(user);
        }
        
        String targetUrl = user.getRole() == Role.ROLE_VETERINARIAN ? "/vet/dashboard" : "/tutor/dashboard";
        response.sendRedirect(targetUrl);
    }
}

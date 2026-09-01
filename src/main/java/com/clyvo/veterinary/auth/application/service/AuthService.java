package com.clyvo.veterinary.auth.application.service;

import com.clyvo.veterinary.auth.application.dto.AuthResponse;
import com.clyvo.veterinary.auth.application.dto.LoginRequest;
import com.clyvo.veterinary.auth.application.dto.RegisterRequest;
import com.clyvo.veterinary.auth.application.port.in.LoginUseCase;
import com.clyvo.veterinary.auth.application.port.in.RegisterUseCase;
import com.clyvo.veterinary.auth.infrastructure.security.JwtService;
import com.clyvo.veterinary.shared.domain.exception.BusinessException;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

@Service
public class AuthService implements RegisterUseCase, LoginUseCase, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já está em uso", 400);
        }
        
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.name(), request.email(), encodedPassword, request.role());
        
        return userRepository.save(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                
        UserDetails userDetails = loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        LocalDateTime expiresAt = jwtService.extractExpiration(token).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        return AuthResponse.bearer(token, user, expiresAt);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
                
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                user.isActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}

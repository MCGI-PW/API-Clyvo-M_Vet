package com.clyvo.veterinary.auth.application.service;

import com.clyvo.veterinary.auth.application.dto.AuthResponse;
import com.clyvo.veterinary.auth.application.dto.LoginRequest;
import com.clyvo.veterinary.auth.application.dto.RegisterRequest;
import com.clyvo.veterinary.auth.infrastructure.security.JwtService;
import com.clyvo.veterinary.shared.domain.exception.BusinessException;
import com.clyvo.veterinary.user.domain.model.Role;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Testes Unitarios")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
                "Dr. Joao Silva",
                "joao@clyvovet.com",
                "senha@123",
                Role.ROLE_VETERINARIAN
        );

        validLoginRequest = new LoginRequest(
                "joao@clyvovet.com",
                "senha@123"
        );
    }

    @Test
    @DisplayName("register — email disponivel — deve criar e retornar usuario")
    void register_emailAvailable_shouldCreateAndReturnUser() {
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("$2a$hashed");
        User savedUser = User.create(
                validRegisterRequest.name(),
                validRegisterRequest.email(),
                "$2a$hashed",
                validRegisterRequest.role()
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(validRegisterRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("joao@clyvovet.com");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_VETERINARIAN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register — email ja cadastrado — deve lancar BusinessException")
    void register_emailAlreadyExists_shouldThrowBusinessException() {
        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login — credenciais validas — deve retornar AuthResponse com token")
    void login_validCredentials_shouldReturnAuthResponseWithToken() {
        User existingUser = User.create("Dr. Joao", "joao@clyvovet.com", "$2a$hashed", Role.ROLE_VETERINARIAN);
        when(userRepository.findByEmail(validLoginRequest.email())).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any())).thenReturn("mocked.jwt.token");
        when(jwtService.extractExpiration("mocked.jwt.token")).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        AuthResponse response = authService.login(validLoginRequest);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("joao@clyvovet.com");
    }

    @Test
    @DisplayName("login — credenciais invalidas — deve lancar BadCredentialsException")
    void login_invalidCredentials_shouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais invalidas"));

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}

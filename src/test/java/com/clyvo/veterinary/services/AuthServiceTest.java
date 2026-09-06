package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ContaAcessoRepository contaRepository;

    @Mock
    private CredencialRepository credencialRepository;

    @Mock
    private IdentificadorAcessoRepository identRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private VeterinarioRepository vetRepository;

    @Mock
    private ClinicaRepository clinicaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private ContaAcesso mockConta;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Carlos Silva");
        registerRequest.setEmail("carlos@vet.com");
        registerRequest.setPassword("senha123");
        registerRequest.setRole("TUTOR");
        registerRequest.setPhone("11999999999");
        registerRequest.setDocument("12345678900");

        mockConta = new ContaAcesso();
        mockConta.setIdConta(UUID.randomUUID());
        mockConta.setEmail("carlos@vet.com");
        mockConta.setTipoConta("TUTOR");
        mockConta.setStatusConta("ATIVA");
    }

    @Test
    @DisplayName("Deve registrar um novo tutor com sucesso")
    void shouldRegisterTutorSuccessfully() {
        when(contaRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(contaRepository.save(any(ContaAcesso.class))).thenReturn(mockConta);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(any(UUID.class), anyString())).thenReturn("mockedJwtToken");
        when(jwtUtil.hashToken(anyString())).thenReturn("mockedTokenHash");
        when(jwtUtil.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        assertEquals("TUTOR", response.getRole());

        verify(contaRepository).save(any(ContaAcesso.class));
        verify(credencialRepository).save(any(Credencial.class));
        verify(tutorRepository).save(any(Tutor.class));
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve registrar um novo veterinario com sucesso")
    void shouldRegisterVeterinarianSuccessfully() {
        registerRequest.setRole("VETERINARIO");
        registerRequest.setCrmv("12345-SP");
        mockConta.setTipoConta("VETERINARIO");

        when(contaRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(contaRepository.save(any(ContaAcesso.class))).thenReturn(mockConta);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(jwtUtil.generateToken(any(UUID.class), anyString())).thenReturn("mockedJwtToken");
        when(jwtUtil.hashToken(anyString())).thenReturn("mockedTokenHash");
        when(jwtUtil.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("VETERINARIO", response.getRole());
        verify(vetRepository).save(any(Veterinario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar com e-mail já existente")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(contaRepository.findByEmail("carlos@vet.com")).thenReturn(Optional.of(mockConta));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email ja em uso", exception.getMessage());
        verify(contaRepository, never()).save(any(ContaAcesso.class));
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais estiverem corretas")
    void shouldLoginSuccessfully() {
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("carlos@vet.com");
        loginRequest.setPassword("senha123");

        Credencial cred = new Credencial();
        cred.setContaAcesso(mockConta);
        cred.setSenhaHash("hashedPassword");

        when(contaRepository.findByEmail("carlos@vet.com")).thenReturn(Optional.of(mockConta));
        when(credencialRepository.findByContaAcessoIdConta(mockConta.getIdConta())).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("senha123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(any(UUID.class), anyString())).thenReturn("mockedJwtToken");
        when(jwtUtil.hashToken(anyString())).thenReturn("mockedTokenHash");
        when(jwtUtil.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 86400000));

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        assertEquals("TUTOR", response.getRole());
        verify(sessaoRepository).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não existir no login")
    void shouldThrowExceptionWhenUserNotFoundOnLogin() {
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("inexistente@vet.com");
        loginRequest.setPassword("senha123");

        when(contaRepository.findByEmail("inexistente@vet.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuario nao encontrado", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha estiver incorreta no login")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmail("carlos@vet.com");
        loginRequest.setPassword("senhaErrada");

        Credencial cred = new Credencial();
        cred.setContaAcesso(mockConta);
        cred.setSenhaHash("hashedPassword");

        when(contaRepository.findByEmail("carlos@vet.com")).thenReturn(Optional.of(mockConta));
        when(credencialRepository.findByContaAcessoIdConta(mockConta.getIdConta())).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("senhaErrada", "hashedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Credenciais invalidas", exception.getMessage());
    }
}

package com.clyvo.veterinary.services;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.dto.AuthRequest;
import com.clyvo.veterinary.dto.AuthResponse;
import com.clyvo.veterinary.dto.RegisterRequest;
import com.clyvo.veterinary.exceptions.AccountBlockedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para autenticação, registro e política de bloqueio no AuthService.
 */
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

    private ContaAcesso mockConta;
    private Credencial mockCred;

    @BeforeEach
    void setUp() {
        mockConta = new ContaAcesso();
        mockConta.setIdConta(UUID.randomUUID());
        mockConta.setEmail("ana@email.com");
        mockConta.setTipoConta("TUTOR");
        mockConta.setStatusConta("ATIVA");

        mockCred = new Credencial();
        mockCred.setIdCredencial(UUID.randomUUID());
        mockCred.setContaAcesso(mockConta);
        mockCred.setSenhaHash("$2a$10$dummyHash");
        mockCred.setTentativasFalhas(0);
        mockCred.setTrocaSenhaObrigatoria(false);
    }

    @Test
    @DisplayName("Deve registrar um novo tutor com sucesso")
    void deveRegistrarTutorComSucesso() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Ana Silva");
        req.setEmail("ana@email.com");
        req.setPassword("senha123");
        req.setRole("TUTOR");
        req.setDocument("12345678900");

        when(contaRepository.findByEmail("ana@email.com")).thenReturn(Optional.empty());
        when(contaRepository.save(any(ContaAcesso.class))).thenAnswer(i -> {
            ContaAcesso c = i.getArgument(0);
            c.setIdConta(UUID.randomUUID());
            return c;
        });
        when(passwordEncoder.encode("senha123")).thenReturn("hashEncoded");
        when(jwtUtil.generateToken(any(), eq("TUTOR"))).thenReturn("mockJwtToken");
        when(jwtUtil.extractExpiration(any())).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtUtil.hashToken(any())).thenReturn("tokenHash");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("mockJwtToken", resp.getToken());
        assertEquals("TUTOR", resp.getRole());

        verify(tutorRepository, times(1)).save(any(Tutor.class));
        verify(credencialRepository, times(1)).save(any(Credencial.class));
        verify(sessaoRepository, times(1)).save(any(Sessao.class));
    }

    @Test
    @DisplayName("Deve recusar cadastro com perfil (role) inválido")
    void deveRejeitarPerfilInvalido() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Hacker");
        req.setEmail("hacker@email.com");
        req.setPassword("123456");
        req.setRole("SUPER_ADMIN_INVALIDO");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(req));
        assertTrue(ex.getMessage().contains("Perfil de usuário inválido"));
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e zerar tentativas falhas")
    void deveFazerLoginComSucesso() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ana@email.com");
        req.setPassword("senha123");

        mockCred.setTentativasFalhas(2);

        when(contaRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mockConta));
        when(credencialRepository.findByContaAcessoIdConta(mockConta.getIdConta())).thenReturn(Optional.of(mockCred));
        when(passwordEncoder.matches("senha123", mockCred.getSenhaHash())).thenReturn(true);
        when(jwtUtil.generateToken(mockConta.getIdConta(), "TUTOR")).thenReturn("validToken");
        when(jwtUtil.extractExpiration("validToken")).thenReturn(new Date(System.currentTimeMillis() + 3600000));
        when(jwtUtil.hashToken("validToken")).thenReturn("hash");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("validToken", resp.getToken());
        assertEquals(0, mockCred.getTentativasFalhas());
        assertNull(mockCred.getBloqueadoAte());
        verify(credencialRepository, times(1)).save(mockCred);
    }

    @Test
    @DisplayName("Deve bloquear conta após 5 tentativas de senha incorreta")
    void deveBloquearAposCincoTentativas() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ana@email.com");
        req.setPassword("senhaErrada");

        mockCred.setTentativasFalhas(4);

        when(contaRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mockConta));
        when(credencialRepository.findByContaAcessoIdConta(mockConta.getIdConta())).thenReturn(Optional.of(mockCred));
        when(passwordEncoder.matches("senhaErrada", mockCred.getSenhaHash())).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> authService.login(req));

        assertEquals("E-mail ou senha incorretos.", ex.getMessage());
        assertEquals(5, mockCred.getTentativasFalhas());
        assertNotNull(mockCred.getBloqueadoAte(), "A conta deve receber uma data de bloqueio.");
        verify(credencialRepository, times(1)).save(mockCred);
    }

    @Test
    @DisplayName("Deve impedir login de conta temporariamente bloqueada")
    void deveImpedirLoginDeContaBloqueada() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ana@email.com");
        req.setPassword("senha123");

        mockCred.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));

        when(contaRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(mockConta));
        when(credencialRepository.findByContaAcessoIdConta(mockConta.getIdConta())).thenReturn(Optional.of(mockCred));

        AccountBlockedException ex = assertThrows(AccountBlockedException.class, () -> authService.login(req));
        assertTrue(ex.getMessage().contains("bloqueada"));
        verify(passwordEncoder, never()).matches(any(), any());
    }
}

package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutorizacaoControllerTest {

    @Mock
    private AutorizacaoAcessoPetRepository autorizacaoRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private ClinicaRepository clinicaRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private AutorizacaoController autorizacaoController;

    private UUID idContaTutor;
    private UUID idTutor;
    private Tutor tutor;
    private Pet pet;
    private Veterinario vet;
    private AutorizacaoAcessoPet auth;

    @BeforeEach
    void setUp() {
        idContaTutor = UUID.randomUUID();
        idTutor = UUID.randomUUID();

        ContaAcesso conta = new ContaAcesso();
        conta.setIdConta(idContaTutor);

        tutor = new Tutor();
        tutor.setIdTutor(idTutor);
        tutor.setNome("Maicon Tutor");
        tutor.setContaAcesso(conta);

        pet = new Pet();
        pet.setIdPet(UUID.randomUUID());
        pet.setNome("Rex");
        pet.setTutor(tutor);

        vet = new Veterinario();
        vet.setIdVeterinario(UUID.randomUUID());
        vet.setNome("Dr. Roberto");
        vet.setContaAcesso(new ContaAcesso());

        auth = new AutorizacaoAcessoPet();
        auth.setIdAutorizacao(UUID.randomUUID());
        auth.setPet(pet);
        auth.setVeterinario(vet);
        auth.setStatus("ATIVA");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(idContaTutor.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar autorizacoes do tutor logado")
    void deveListarAutorizacoesDoTutor() {
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(tutor));
        when(autorizacaoRepository.findByPetTutorIdTutor(idTutor)).thenReturn(List.of(auth));

        ResponseEntity<List<AutorizacaoAcessoPet>> response = autorizacaoController.listAutorizacoes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(auth.getIdAutorizacao(), response.getBody().get(0).getIdAutorizacao());
    }

    @Test
    @DisplayName("Tutor deve revogar autorizacao cancelando consultas ativas associadas")
    void deveRevogarAutorizacaoECancelarConsultas() {
        Consulta consultaAgendada = new Consulta();
        consultaAgendada.setStatus("AGENDADO");
        consultaAgendada.setVeterinario(vet);

        when(autorizacaoRepository.findById(auth.getIdAutorizacao())).thenReturn(Optional.of(auth));
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(tutor));
        when(consultaRepository.findByPetIdPet(pet.getIdPet())).thenReturn(List.of(consultaAgendada));

        ResponseEntity<?> response = autorizacaoController.revogarAutorizacao(auth.getIdAutorizacao(), Map.of("motivo", "Troca de clínica"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("REVOGADA", auth.getStatus());
        assertEquals("CANCELADA", consultaAgendada.getStatus());
        verify(autorizacaoRepository, times(1)).save(auth);
        verify(consultaRepository, times(1)).save(consultaAgendada);
        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Nao deve permitir tutor revogar autorizacao de outro tutor (403)")
    void naoDeveRevogarAutorizacaoDeOutroTutor() {
        Tutor outroTutor = new Tutor();
        outroTutor.setIdTutor(UUID.randomUUID());

        when(autorizacaoRepository.findById(auth.getIdAutorizacao())).thenReturn(Optional.of(auth));
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(outroTutor));

        ResponseEntity<?> response = autorizacaoController.revogarAutorizacao(auth.getIdAutorizacao(), Collections.emptyMap());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(autorizacaoRepository, never()).save(any());
    }
}

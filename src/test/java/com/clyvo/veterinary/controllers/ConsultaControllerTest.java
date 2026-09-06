package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.NotificacaoRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import com.clyvo.veterinary.repositories.VeterinarioRepository;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsultaControllerTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @InjectMocks
    private ConsultaController consultaController;

    private UUID idConta;
    private UUID idConsulta;
    private UUID idTutor;
    private Tutor tutor;
    private Pet pet;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();
        idConsulta = UUID.randomUUID();
        idTutor = UUID.randomUUID();

        ContaAcesso conta = new ContaAcesso();
        conta.setIdConta(idConta);

        tutor = new Tutor();
        tutor.setIdTutor(idTutor);
        tutor.setNome("Maicon Tutor");
        tutor.setContaAcesso(conta);

        pet = new Pet();
        pet.setIdPet(UUID.randomUUID());
        pet.setNome("Rex");
        pet.setTutor(tutor);

        consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        consulta.setPet(pet);
        consulta.setDataHora(LocalDateTime.now().plusDays(1));
        consulta.setModalidade("ONLINE");
        consulta.setStatus("AGENDADO");

        // Mock Security Context
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(idConta.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tutor deve cancelar consulta agendada com sucesso gerando notificacao")
    void deveCancelarConsultaComSucesso() {
        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consulta));
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(tutor));

        ResponseEntity<?> response = consultaController.cancelarConsulta(idConsulta);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CANCELADA", consulta.getStatus());
        verify(consultaRepository, times(1)).save(consulta);
        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Nao deve cancelar consulta inexistente (404)")
    void naoDeveCancelarConsultaInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(consultaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        ResponseEntity<?> response = consultaController.cancelarConsulta(idInexistente);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve cancelar consulta ja concluida (400)")
    void naoDeveCancelarConsultaJaConcluida() {
        consulta.setStatus("CONCLUIDA");
        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consulta));

        ResponseEntity<?> response = consultaController.cancelarConsulta(idConsulta);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve permitir tutor cancelar consulta de outro tutor (403)")
    void naoDevePermitirCancelarConsultaDeOutroTutor() {
        Tutor outroTutor = new Tutor();
        outroTutor.setIdTutor(UUID.randomUUID());

        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consulta));
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(outroTutor));

        ResponseEntity<?> response = consultaController.cancelarConsulta(idConsulta);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar consultas do tutor logado")
    void deveListarConsultasDoTutor() {
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(tutor));
        when(consultaRepository.findByPetTutorIdTutor(idTutor)).thenReturn(List.of(consulta));

        ResponseEntity<List<Consulta>> response = consultaController.listConsultas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(idConsulta, response.getBody().get(0).getIdConsulta());
    }
}

package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.ConsultaRequest;
import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import com.clyvo.veterinary.services.ConsultaService;
import com.clyvo.veterinary.services.NotificacaoService;
import com.clyvo.veterinary.services.TitularidadeConsulta;
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

    @Mock
    private ClinicaRepository clinicaRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private AutorizacaoAcessoPetRepository autorizacaoRepository;

    @Mock
    private VeterinarioClinicaRepository vcRepository;

    @Mock
    private com.clyvo.veterinary.repositories.ProntuarioRepository prontuarioRepository;

    private ConsultaController consultaController;

    private UUID idConta;
    private UUID idConsulta;
    private UUID idTutor;
    private Tutor tutor;
    private Pet pet;
    private Veterinario vet;
    private Clinica clinica;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        ConsultaService consultaService = new ConsultaService(consultaRepository, tutorRepository, veterinarioRepository,
                clinicaRepository, petRepository, autorizacaoRepository, vcRepository,
                new NotificacaoService(notificacaoRepository), prontuarioRepository,
                new TitularidadeConsulta(tutorRepository, veterinarioRepository, clinicaRepository));
        consultaController = new ConsultaController(consultaService);

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

        vet = new Veterinario();
        vet.setIdVeterinario(UUID.randomUUID());
        vet.setNome("Dr. Roberto");
        vet.setContaAcesso(conta);

        clinica = new Clinica();
        clinica.setIdClinica(UUID.randomUUID());
        clinica.setNomeFantasia("Clyvo Central");

        consulta = new Consulta();
        consulta.setIdConsulta(idConsulta);
        consulta.setPet(pet);
        consulta.setVeterinario(vet);
        consulta.setClinica(clinica);
        consulta.setDataHora(LocalDateTime.now().plusDays(1));
        consulta.setModalidade("TELEMEDICINA");
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
    @DisplayName("Deve agendar consulta e gerar automaticamente autorizacao de acesso ao pet")
    void deveAgendarConsultaEGerarAutorizacao() {
        pet.setAtivo(true);
        clinica.setAtiva(true);
        VeterinarioClinica vinculo = new VeterinarioClinica();
        vinculo.setVeterinario(vet);
        vinculo.setClinica(clinica);
        vinculo.setStatusVinculo("ATIVO");

        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(tutor));
        when(petRepository.findById(any())).thenReturn(Optional.of(pet));
        when(veterinarioRepository.findById(any())).thenReturn(Optional.of(vet));
        when(clinicaRepository.findById(any())).thenReturn(Optional.of(clinica));
        when(vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(any(), any())).thenReturn(Optional.of(vinculo));
        when(consultaRepository.findByVeterinarioIdVeterinarioAndDataHora(any(), any())).thenReturn(Collections.emptyList());
        when(autorizacaoRepository.findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(any(), any(), eq("ATIVA")))
                .thenReturn(Optional.empty());

        ConsultaRequest request = new ConsultaRequest(
                new ConsultaRequest.PetRef(pet.getIdPet()),
                new ConsultaRequest.VeterinarioRef(vet.getIdVeterinario()),
                new ConsultaRequest.ClinicaRef(clinica.getIdClinica()),
                LocalDateTime.now().plusDays(1),
                "TELEMEDICINA");

        ResponseEntity<Void> response = consultaController.createConsulta(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
        verify(autorizacaoRepository, times(1)).save(any(AutorizacaoAcessoPet.class));
        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
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

        // O GlobalExceptionHandler converte ResourceNotFoundException em HTTP 404
        assertThrows(ResourceNotFoundException.class, () -> consultaController.cancelarConsulta(idInexistente));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve cancelar consulta ja concluida (400)")
    void naoDeveCancelarConsultaJaConcluida() {
        consulta.setStatus("CONCLUIDA");
        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consulta));

        // O GlobalExceptionHandler converte BusinessException em HTTP 400
        assertThrows(BusinessException.class, () -> consultaController.cancelarConsulta(idConsulta));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve permitir tutor cancelar consulta de outro tutor (403)")
    void naoDevePermitirCancelarConsultaDeOutroTutor() {
        Tutor outroTutor = new Tutor();
        outroTutor.setIdTutor(UUID.randomUUID());

        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consulta));
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(outroTutor));

        // O GlobalExceptionHandler converte AccessDeniedException em HTTP 403
        assertThrows(AccessDeniedException.class, () -> consultaController.cancelarConsulta(idConsulta));
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

    @Test
    @DisplayName("Deve listar consultas isoladas da clinica logada (Multi-Tenancy)")
    void deveListarConsultasIsoladasDaClinica() {
        when(clinicaRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(clinica));
        when(consultaRepository.findByClinicaIdClinica(clinica.getIdClinica())).thenReturn(List.of(consulta));

        ResponseEntity<List<Consulta>> response = consultaController.listConsultas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(consultaRepository, times(1)).findByClinicaIdClinica(clinica.getIdClinica());
    }
}

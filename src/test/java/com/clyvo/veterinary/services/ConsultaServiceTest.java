package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários pedagógicos do ConsultaService com padrão AAA (Arrange, Act, Assert).
 */
@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private TutorRepository tutorRepository;

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
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private UUID idContaTutor;
    private Tutor mockTutor;
    private Pet mockPet;
    private Veterinario mockVet;
    private Clinica mockClinica;
    private Consulta mockConsulta;
    private VeterinarioClinica mockVinculo;

    @BeforeEach
    void setUp() {
        idContaTutor = UUID.randomUUID();

        mockTutor = new Tutor();
        mockTutor.setIdTutor(UUID.randomUUID());
        mockTutor.setNome("Carlos Tutor");
        ContaAcesso contaTutor = new ContaAcesso();
        contaTutor.setIdConta(idContaTutor);
        mockTutor.setContaAcesso(contaTutor);

        mockPet = new Pet();
        mockPet.setIdPet(UUID.randomUUID());
        mockPet.setNome("Rex");
        mockPet.setTutor(mockTutor);
        mockPet.setAtivo(true);

        mockVet = new Veterinario();
        mockVet.setIdVeterinario(UUID.randomUUID());
        mockVet.setNome("Dr. Roberto Silveira");

        mockClinica = new Clinica();
        mockClinica.setIdClinica(UUID.randomUUID());
        mockClinica.setNomeFantasia("Clyvo Central");
        mockClinica.setAtiva(true);

        mockVinculo = new VeterinarioClinica();
        mockVinculo.setVeterinario(mockVet);
        mockVinculo.setClinica(mockClinica);
        mockVinculo.setStatusVinculo("ATIVO");

        mockConsulta = new Consulta();
        mockConsulta.setIdConsulta(UUID.randomUUID());
        mockConsulta.setPet(mockPet);
        mockConsulta.setVeterinario(mockVet);
        mockConsulta.setClinica(mockClinica);
        mockConsulta.setDataHora(LocalDateTime.now().plusDays(2));
        mockConsulta.setStatus("AGENDADO");
        mockConsulta.setModalidade("PRESENCIAL");
    }

    @Test
    @DisplayName("Deve agendar uma consulta com sucesso quando dados e regras forem válidos")
    void deveCriarConsultaComSucesso() {
        // Arrange
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(mockTutor));
        when(petRepository.findById(mockPet.getIdPet())).thenReturn(Optional.of(mockPet));
        when(veterinarioRepository.findById(mockVet.getIdVeterinario())).thenReturn(Optional.of(mockVet));
        when(clinicaRepository.findById(mockClinica.getIdClinica())).thenReturn(Optional.of(mockClinica));
        when(vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(mockVet.getIdVeterinario(), mockClinica.getIdClinica()))
                .thenReturn(Optional.of(mockVinculo));
        when(consultaRepository.findByVeterinarioIdVeterinarioAndDataHora(mockVet.getIdVeterinario(), mockConsulta.getDataHora()))
                .thenReturn(Collections.emptyList());
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(autorizacaoRepository.findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(any(), any(), eq("ATIVA")))
                .thenReturn(Optional.empty());

        // Act
        Consulta resultado = consultaService.createConsulta(mockConsulta, idContaTutor);

        // Assert
        assertNotNull(resultado);
        assertEquals("AGENDADO", resultado.getStatus());
        assertEquals(mockPet, resultado.getPet());
        assertEquals(mockVet, resultado.getVeterinario());

        verify(consultaRepository, times(1)).save(any(Consulta.class));
        verify(autorizacaoRepository, times(1)).save(any(AutorizacaoAcessoPet.class));
        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException se o pet pertencer a outro tutor")
    void deveLancarExcecaoQuandoPetNaoPertencerAoTutorAutenticado() {
        // Arrange
        Tutor outroTutor = new Tutor();
        outroTutor.setIdTutor(UUID.randomUUID());
        outroTutor.setNome("Outro Dono");
        mockPet.setTutor(outroTutor);

        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(mockTutor));
        when(petRepository.findById(mockPet.getIdPet())).thenReturn(Optional.of(mockPet));

        // Act & Assert
        AccessDeniedException ex = assertThrows(AccessDeniedException.class, () -> {
            consultaService.createConsulta(mockConsulta, idContaTutor);
        });

        assertTrue(ex.getMessage().contains("não pertence ao tutor autenticado"));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException se houver conflito de horário para o veterinário")
    void deveLancarExcecaoQuandoHouverConflitoDeHorario() {
        // Arrange
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(mockTutor));
        when(petRepository.findById(mockPet.getIdPet())).thenReturn(Optional.of(mockPet));
        when(veterinarioRepository.findById(mockVet.getIdVeterinario())).thenReturn(Optional.of(mockVet));
        when(clinicaRepository.findById(mockClinica.getIdClinica())).thenReturn(Optional.of(mockClinica));
        when(vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(mockVet.getIdVeterinario(), mockClinica.getIdClinica()))
                .thenReturn(Optional.of(mockVinculo));

        Consulta conflito = new Consulta();
        conflito.setStatus("AGENDADO");
        when(consultaRepository.findByVeterinarioIdVeterinarioAndDataHora(mockVet.getIdVeterinario(), mockConsulta.getDataHora()))
                .thenReturn(List.of(conflito));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            consultaService.createConsulta(mockConsulta, idContaTutor);
        });

        assertTrue(ex.getMessage().contains("já possui uma consulta agendada"));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException se data da consulta for no passado")
    void deveLancarExcecaoQuandoDataNoPassado() {
        // Arrange
        mockConsulta.setDataHora(LocalDateTime.now().minusDays(1));
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(mockTutor));
        when(petRepository.findById(mockPet.getIdPet())).thenReturn(Optional.of(mockPet));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            consultaService.createConsulta(mockConsulta, idContaTutor);
        });

        assertTrue(ex.getMessage().contains("não pode ser no passado"));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar consulta agendada quando tutor for autorizado")
    void deveCancelarConsultaQuandoTutorAutorizado() {
        // Arrange
        UUID idConsulta = mockConsulta.getIdConsulta();
        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(mockConsulta));
        when(tutorRepository.findByContaAcessoIdConta(idContaTutor)).thenReturn(Optional.of(mockTutor));

        // Act
        consultaService.cancelarConsulta(idConsulta, idContaTutor);

        // Assert
        assertEquals("CANCELADA", mockConsulta.getStatus());
        verify(consultaRepository, times(1)).save(mockConsulta);
        verify(notificacaoRepository, atLeastOnce()).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("Deve concluir consulta e persistir Prontuário médico com sucesso")
    void deveConcluirConsultaEPersistirProntuario() {
        // Arrange
        UUID idConsulta = mockConsulta.getIdConsulta();
        UUID idContaVet = UUID.randomUUID();
        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(mockConsulta));
        when(veterinarioRepository.findByContaAcessoIdConta(idContaVet)).thenReturn(Optional.of(mockVet));
        when(prontuarioRepository.findByConsultaIdConsulta(idConsulta)).thenReturn(Optional.empty());

        // Act
        consultaService.completeConsulta(idConsulta, idContaVet, "Animal examinado, administrada vacina V10.");

        // Assert
        assertEquals("CONCLUIDA", mockConsulta.getStatus());
        verify(consultaRepository, times(1)).save(mockConsulta);
        verify(prontuarioRepository, times(1)).save(any(Prontuario.class));
        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
    }
}

package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.repositories.*;
import com.clyvo.veterinary.services.ia.AssistenteClinico;
import com.clyvo.veterinary.services.ia.ClaudeAssistenteClinico;
import com.clyvo.veterinary.services.ia.ClyvoScribeAssistenteClinico;
import com.clyvo.veterinary.services.ia.ResumoClinico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Testes do fluxo de transcrição + resumo por IA, com padrão AAA e a IA externa simulada. */
@ExtendWith(MockitoExtension.class)
class TranscricaoServiceTest {

    private static final String TRANSCRICAO = "Parece ser uma otite. Vou prescrever 3 gotas duas vezes ao dia. Retorno em 10 dias.";

    @Mock private ConsultaRepository consultaRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private VeterinarioRepository veterinarioRepository;
    @Mock private ClinicaRepository clinicaRepository;
    @Mock private TranscricaoConsultaRepository transcricaoRepository;
    @Mock private AssistenteClinico assistenteIA;

    private TitularidadeConsulta titularidade;
    private TranscricaoService service;
    private Consulta consulta;
    private UUID idContaVet;

    @BeforeEach
    void setUp() {
        titularidade = new TitularidadeConsulta(tutorRepository, veterinarioRepository, clinicaRepository);
        service = new TranscricaoService(consultaRepository, titularidade, transcricaoRepository, assistenteIA);

        Veterinario vet = new Veterinario();
        vet.setIdVeterinario(UUID.randomUUID());
        Pet pet = new Pet();
        pet.setNome("Thor");

        consulta = new Consulta();
        consulta.setIdConsulta(UUID.randomUUID());
        consulta.setVeterinario(vet);
        consulta.setPet(pet);
        consulta.setStatus(StatusConsulta.AGENDADO.name());

        idContaVet = UUID.randomUUID();
        lenient().when(consultaRepository.findById(consulta.getIdConsulta())).thenReturn(Optional.of(consulta));
        lenient().when(veterinarioRepository.findByContaAcessoIdConta(idContaVet)).thenReturn(Optional.of(vet));
        lenient().when(transcricaoRepository.save(any(TranscricaoConsulta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Grava a transcrição com as sugestões estruturadas da IA")
    void geraResumoComIA() {
        when(assistenteIA.resumir(anyString(), anyString())).thenReturn(new ResumoClinico(
                "Otite externa", "Solução otológica 3 gotas 2x/dia por 7 dias", "Retorno em 10 dias",
                "O Thor está com uma inflamação no ouvido.", List.of("Febre", "Parar de comer")));
        when(assistenteIA.fonte()).thenReturn("CLAUDE:claude-opus-5-5");

        TranscricaoConsulta resultado = service.gerarResumo(consulta.getIdConsulta(), idContaVet, TRANSCRICAO);

        assertEquals("Otite externa", resultado.getDiagnosticoSugerido());
        assertEquals("Febre\nParar de comer", resultado.getSinaisAlerta());
        assertEquals("CLAUDE:claude-opus-5-5", resultado.getFonteIa());
        assertEquals(TRANSCRICAO, resultado.getTextoTranscricao());
        verify(assistenteIA).resumir(TRANSCRICAO, "Thor");
    }

    @Test
    @DisplayName("Se a IA externa falhar, usa o assistente local e não interrompe a consulta")
    void usaAssistenteLocalQuandoIAFalha() {
        when(assistenteIA.resumir(anyString(), anyString())).thenThrow(new RuntimeException("timeout"));
        when(assistenteIA.fonte()).thenReturn("CLAUDE:claude-opus-5-5");

        TranscricaoConsulta resultado = service.gerarResumo(consulta.getIdConsulta(), idContaVet, TRANSCRICAO);

        assertEquals("LOCAL", resultado.getFonteIa());
        assertTrue(resultado.getPrescricaoSugerida().contains("3 gotas"));
    }

    @Test
    @DisplayName("Sem chave de API configurada, o resumo é feito pelo assistente local")
    void semChaveUsaAssistenteLocal() {
        TranscricaoService semIA = new TranscricaoService(consultaRepository, titularidade,
                transcricaoRepository, (AssistenteClinico) null);

        TranscricaoConsulta resultado = semIA.gerarResumo(consulta.getIdConsulta(), idContaVet, TRANSCRICAO);

        assertEquals("LOCAL", resultado.getFonteIa());
    }

    @Test
    @DisplayName("Escolhe a IA pela configuração: ClyvoScribe, depois Claude, senão só o assistente local")
    void escolheAssistentePelaConfiguracao() {
        assertInstanceOf(ClyvoScribeAssistenteClinico.class,
                TranscricaoService.escolherAssistente("http://localhost:8000", "chave", "claude-opus-5-5"));
        assertInstanceOf(ClaudeAssistenteClinico.class,
                TranscricaoService.escolherAssistente(" ", "chave", "claude-opus-5-5"));
        assertNull(TranscricaoService.escolherAssistente("", "", "claude-opus-5-5"));
    }

    @Test
    @DisplayName("Veterinário que não é o responsável não pode transcrever a consulta")
    void bloqueiaVeterinarioDeOutraConsulta() {
        UUID outraConta = UUID.randomUUID();
        Veterinario outro = new Veterinario();
        outro.setIdVeterinario(UUID.randomUUID());
        when(veterinarioRepository.findByContaAcessoIdConta(outraConta)).thenReturn(Optional.of(outro));

        assertThrows(AccessDeniedException.class,
                () -> service.gerarResumo(consulta.getIdConsulta(), outraConta, TRANSCRICAO));
        verifyNoInteractions(assistenteIA, transcricaoRepository);
    }

    @Test
    @DisplayName("Consulta cancelada ou transcrição vazia não geram resumo")
    void rejeitaCanceladaOuVazia() {
        assertThrows(BusinessException.class,
                () -> service.gerarResumo(consulta.getIdConsulta(), idContaVet, "   "));

        consulta.setStatus(StatusConsulta.CANCELADA.name());
        assertThrows(BusinessException.class,
                () -> service.gerarResumo(consulta.getIdConsulta(), idContaVet, TRANSCRICAO));
        verifyNoInteractions(assistenteIA);
    }
}

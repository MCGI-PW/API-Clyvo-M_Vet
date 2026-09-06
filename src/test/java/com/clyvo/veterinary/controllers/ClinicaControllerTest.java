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
class ClinicaControllerTest {

    @Mock
    private ClinicaRepository clinicaRepository;

    @Mock
    private VeterinarioClinicaRepository vcRepository;

    @Mock
    private VeterinarioRepository vetRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private AutorizacaoAcessoPetRepository autorizacaoRepository;

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @InjectMocks
    private ClinicaController clinicaController;

    private UUID idContaClinica;
    private UUID idClinica;
    private Clinica clinica;
    private Veterinario vet1;
    private Veterinario vet2;

    @BeforeEach
    void setUp() {
        idContaClinica = UUID.randomUUID();
        idClinica = UUID.randomUUID();

        ContaAcesso conta = new ContaAcesso();
        conta.setIdConta(idContaClinica);

        clinica = new Clinica();
        clinica.setIdClinica(idClinica);
        clinica.setContaAcesso(conta);
        clinica.setNomeFantasia("Clyvo Central");
        clinica.setAtiva(true);

        vet1 = new Veterinario();
        vet1.setIdVeterinario(UUID.randomUUID());
        vet1.setNome("Dr. Roberto");

        vet2 = new Veterinario();
        vet2.setIdVeterinario(UUID.randomUUID());
        vet2.setNome("Dra. Camila");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(idContaClinica.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve listar clinicas ativas para selecao no agendamento")
    void deveListarClinicasAtivas() {
        when(clinicaRepository.findAll()).thenReturn(List.of(clinica));

        ResponseEntity<List<Clinica>> response = clinicaController.listClinicasAtivas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(idClinica, response.getBody().get(0).getIdClinica());
    }

    @Test
    @DisplayName("Deve vincular veterinario a equipe da clinica")
    void deveVincularVeterinarioAClinica() {
        when(clinicaRepository.findByContaAcessoIdConta(idContaClinica)).thenReturn(Optional.of(clinica));
        when(vetRepository.findById(vet1.getIdVeterinario())).thenReturn(Optional.of(vet1));
        when(vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(vet1.getIdVeterinario(), idClinica))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = clinicaController.vincularVeterinario(Map.of("idVeterinario", vet1.getIdVeterinario().toString()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(vcRepository, times(1)).save(any(VeterinarioClinica.class));
    }

    @Test
    @DisplayName("Deve transferir autorizacao e consultas de um veterinario para outro na mesma clinica")
    void deveTransferirAutorizacaoEntreVeterinarios() {
        UUID idAuth = UUID.randomUUID();
        Pet pet = new Pet();
        pet.setIdPet(UUID.randomUUID());
        pet.setNome("Bob");

        AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
        auth.setIdAutorizacao(idAuth);
        auth.setClinica(clinica);
        auth.setVeterinario(vet1);
        auth.setPet(pet);
        auth.setStatus("ATIVA");

        VeterinarioClinica vcAtivo = new VeterinarioClinica();
        vcAtivo.setStatusVinculo("ATIVO");

        when(clinicaRepository.findByContaAcessoIdConta(idContaClinica)).thenReturn(Optional.of(clinica));
        when(autorizacaoRepository.findById(idAuth)).thenReturn(Optional.of(auth));
        when(vetRepository.findById(vet2.getIdVeterinario())).thenReturn(Optional.of(vet2));
        when(vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(vet2.getIdVeterinario(), idClinica))
                .thenReturn(Optional.of(vcAtivo));
        when(consultaRepository.findByPetIdPet(pet.getIdPet())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = clinicaController.transferirAutorizacao(idAuth, Map.of("idNovoVeterinario", vet2.getIdVeterinario().toString()));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(vet2.getIdVeterinario(), auth.getVeterinario().getIdVeterinario());
        verify(autorizacaoRepository, times(1)).save(auth);
    }
}

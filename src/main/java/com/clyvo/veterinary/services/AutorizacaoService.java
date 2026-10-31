package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.StatusAutorizacao;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AutorizacaoService {

    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final ConsultaRepository consultaRepository;
    private final NotificacaoService notificacaoService;
    private final PetRepository petRepository;

    public AutorizacaoService(AutorizacaoAcessoPetRepository autorizacaoRepository,
                              TutorRepository tutorRepository,
                              VeterinarioRepository veterinarioRepository,
                              ClinicaRepository clinicaRepository,
                              ConsultaRepository consultaRepository,
                              NotificacaoService notificacaoService,
                              PetRepository petRepository) {
        this.autorizacaoRepository = autorizacaoRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.consultaRepository = consultaRepository;
        this.notificacaoService = notificacaoService;
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<AutorizacaoAcessoPet> listAutorizacoesByConta(UUID idConta) {
        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return autorizacaoRepository.findByPetTutorIdTutor(tutorOpt.get().getIdTutor());
        }

        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return autorizacaoRepository.findByVeterinarioIdVeterinarioAndStatus(vetOpt.get().getIdVeterinario(), StatusAutorizacao.ATIVA.name());
        }

        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);
        if (clinicaOpt.isPresent()) {
            return autorizacaoRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        }

        return Collections.emptyList();
    }

    @Transactional
    public void revogarAutorizacao(UUID idAutorizacao, UUID idConta, String motivo) {
        AutorizacaoAcessoPet auth = autorizacaoRepository.findById(idAutorizacao)
                .orElseThrow(() -> new ResourceNotFoundException("Autorização não encontrada com ID: " + idAutorizacao));

        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .filter(t -> auth.getPet() != null && auth.getPet().pertenceA(t))
                .orElseThrow(() -> new AccessDeniedException("Acesso negado: apenas o tutor responsável pelo pet pode revogar esta autorização."));

        auth.setStatus(StatusAutorizacao.REVOGADA.name());
        auth.setMotivoRevogacao((motivo != null && !motivo.isBlank()) ? motivo : "Revogado pelo tutor.");
        autorizacaoRepository.save(auth);

        // Sem consentimento, as consultas agendadas com o médico/clínica revogados são canceladas
        consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> StatusConsulta.AGENDADO.matches(c.getStatus()))
                .filter(c -> mesmoVeterinario(c, auth) || mesmaClinica(c, auth))
                .forEach(c -> {
                    c.setStatus(StatusConsulta.CANCELADA.name());
                    consultaRepository.save(c);
                });

        String destinatario = auth.getVeterinario() != null
                ? "do Dr(a). " + auth.getVeterinario().getNome()
                : "da clínica " + auth.getClinica().getNomeFantasia();
        notificacaoService.notificar(tutor.getContaAcesso(),
                "Autorização de acesso " + destinatario + " ao pet " + auth.getPet().getNome() + " foi revogada com sucesso.");
        if (auth.getVeterinario() != null) {
            notificacaoService.notificar(auth.getVeterinario().getContaAcesso(),
                    "A autorização de acesso ao pet " + auth.getPet().getNome() + " foi revogada pelo tutor. Consultas pendentes foram canceladas.");
        }
    }

    @Transactional
    public AutorizacaoAcessoPet concederAutorizacao(UUID idPet, UUID idVeterinario, UUID idClinica, UUID idConta) {
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new AccessDeniedException("Apenas contas de tutores podem conceder autorizações de prontuário."));

        Pet pet = petRepository.findById(idPet)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com ID: " + idPet));

        if (!pet.pertenceA(tutor)) {
            throw new AccessDeniedException("Acesso negado: o pet informado não pertence ao tutor autenticado.");
        }

        if (idVeterinario == null) {
            throw new BusinessException("Informe o médico veterinário que receberá acesso ao prontuário.");
        }
        Veterinario vet = veterinarioRepository.findById(idVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Médico veterinário não encontrado."));
        Clinica clinica = idClinica == null ? null : clinicaRepository.findById(idClinica)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada."));

        Optional<AutorizacaoAcessoPet> existente = buscarAutorizacaoAtiva(pet, vet);
        if (existente.isPresent()) {
            return existente.get();
        }

        AutorizacaoAcessoPet salva = autorizacaoRepository.save(
                AutorizacaoAcessoPet.ativa(pet, vet, clinica, tutor.getContaAcesso()));
        notificacaoService.notificar(vet.getContaAcesso(),
                "Você recebeu autorização de acesso ao prontuário do paciente " + pet.getNome() + ".");
        return salva;
    }

    private Optional<AutorizacaoAcessoPet> buscarAutorizacaoAtiva(Pet pet, Veterinario vet) {
        return autorizacaoRepository.findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(
                pet.getIdPet(), vet.getIdVeterinario(), StatusAutorizacao.ATIVA.name());
    }

    private static boolean mesmoVeterinario(Consulta c, AutorizacaoAcessoPet auth) {
        return auth.getVeterinario() != null && c.getVeterinario() != null
                && c.getVeterinario().getIdVeterinario().equals(auth.getVeterinario().getIdVeterinario());
    }

    private static boolean mesmaClinica(Consulta c, AutorizacaoAcessoPet auth) {
        return auth.getClinica() != null && c.getClinica() != null
                && c.getClinica().getIdClinica().equals(auth.getClinica().getIdClinica());
    }
}

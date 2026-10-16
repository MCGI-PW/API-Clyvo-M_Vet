package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutorizacaoService {

    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final PetRepository petRepository;

    public AutorizacaoService(AutorizacaoAcessoPetRepository autorizacaoRepository,
                              TutorRepository tutorRepository,
                              VeterinarioRepository veterinarioRepository,
                              ClinicaRepository clinicaRepository,
                              ConsultaRepository consultaRepository,
                              NotificacaoRepository notificacaoRepository,
                              PetRepository petRepository) {
        this.autorizacaoRepository = autorizacaoRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.consultaRepository = consultaRepository;
        this.notificacaoRepository = notificacaoRepository;
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
            return autorizacaoRepository.findByVeterinarioIdVeterinarioAndStatus(vetOpt.get().getIdVeterinario(), "ATIVA");
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
                .orElseThrow(() -> new com.clyvo.veterinary.exceptions.ResourceNotFoundException("Autorização não encontrada com ID: " + idAutorizacao));

        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isEmpty() || auth.getPet() == null || auth.getPet().getTutor() == null ||
                !auth.getPet().getTutor().getIdTutor().equals(tutorOpt.get().getIdTutor())) {
            throw new com.clyvo.veterinary.exceptions.AccessDeniedException("Acesso negado: apenas o tutor responsável pelo pet pode revogar esta autorização.");
        }

        auth.setStatus("REVOGADA");
        auth.setMotivoRevogacao((motivo != null && !motivo.isBlank()) ? motivo : "Revogado pelo tutor.");
        autorizacaoRepository.save(auth);

        // Cancelar consultas agendadas associadas ao médico ou à clínica
        List<Consulta> consultas = consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> "AGENDADO".equalsIgnoreCase(c.getStatus()) &&
                        ((auth.getVeterinario() != null && c.getVeterinario() != null && c.getVeterinario().getIdVeterinario().equals(auth.getVeterinario().getIdVeterinario())) ||
                         (auth.getClinica() != null && c.getClinica() != null && c.getClinica().getIdClinica().equals(auth.getClinica().getIdClinica()))))
                .collect(Collectors.toList());

        for (Consulta c : consultas) {
            c.setStatus("CANCELADA");
            consultaRepository.save(c);
        }

        // Notificacao Tutor
        Notificacao notifTutor = new Notificacao();
        notifTutor.setContaAcesso(tutorOpt.get().getContaAcesso());
        notifTutor.setMensagem("Autorização de acesso do Dr(a). " + auth.getVeterinario().getNome() + " ao pet " + auth.getPet().getNome() + " foi revogada com sucesso.");
        notificacaoRepository.save(notifTutor);

        // Notificacao Veterinário
        if (auth.getVeterinario().getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(auth.getVeterinario().getContaAcesso());
            notifVet.setMensagem("A autorização de acesso ao pet " + auth.getPet().getNome() + " foi revogada pelo tutor. Consultas pendentes foram canceladas.");
            notificacaoRepository.save(notifVet);
        }
    }

    @Transactional
    public AutorizacaoAcessoPet concederAutorizacao(UUID idPet, UUID idVeterinario, UUID idClinica, UUID idConta) {
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new com.clyvo.veterinary.exceptions.AccessDeniedException("Apenas contas de tutores podem conceder autorizações de prontuário."));

        Pet pet = petRepository.findById(idPet)
                .orElseThrow(() -> new com.clyvo.veterinary.exceptions.ResourceNotFoundException("Pet não encontrado com ID: " + idPet));

        if (pet.getTutor() == null || !pet.getTutor().getIdTutor().equals(tutor.getIdTutor())) {
            throw new com.clyvo.veterinary.exceptions.AccessDeniedException("Acesso negado: o pet informado não pertence ao tutor autenticado.");
        }

        Veterinario vet = null;
        if (idVeterinario != null) {
            vet = veterinarioRepository.findById(idVeterinario)
                    .orElseThrow(() -> new com.clyvo.veterinary.exceptions.ResourceNotFoundException("Médico veterinário não encontrado."));
        }

        Clinica clinica = null;
        if (idClinica != null) {
            clinica = clinicaRepository.findById(idClinica)
                    .orElseThrow(() -> new com.clyvo.veterinary.exceptions.ResourceNotFoundException("Clínica não encontrada."));
        }

        if (vet == null && clinica == null) {
            throw new com.clyvo.veterinary.exceptions.BusinessException("Informe ao menos um médico veterinário ou uma clínica para conceder acesso.");
        }

        // Se já existe autorização ativa, não duplica
        Optional<AutorizacaoAcessoPet> existente = (vet != null)
                ? autorizacaoRepository.findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(pet.getIdPet(), vet.getIdVeterinario(), "ATIVA")
                : autorizacaoRepository.findFirstByPetIdPetAndClinicaIdClinicaAndStatus(pet.getIdPet(), clinica.getIdClinica(), "ATIVA");

        if (existente.isPresent()) {
            return existente.get();
        }

        AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
        auth.setPet(pet);
        auth.setVeterinario(vet);
        auth.setClinica(clinica);
        auth.setContaAutorizador(tutor.getContaAcesso());
        auth.setDataAutorizacao(java.time.LocalDateTime.now());
        auth.setStatus("ATIVA");
        AutorizacaoAcessoPet salva = autorizacaoRepository.save(auth);

        if (vet != null && vet.getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(vet.getContaAcesso());
            notifVet.setMensagem("Você recebeu autorização de acesso ao prontuário do paciente " + pet.getNome() + ".");
            notificacaoRepository.save(notifVet);
        }

        return salva;
    }
}

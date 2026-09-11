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

    public AutorizacaoService(AutorizacaoAcessoPetRepository autorizacaoRepository,
                              TutorRepository tutorRepository,
                              VeterinarioRepository veterinarioRepository,
                              ClinicaRepository clinicaRepository,
                              ConsultaRepository consultaRepository,
                              NotificacaoRepository notificacaoRepository) {
        this.autorizacaoRepository = autorizacaoRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.consultaRepository = consultaRepository;
        this.notificacaoRepository = notificacaoRepository;
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
}

package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PetRepository petRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final VeterinarioClinicaRepository vcRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final ProntuarioRepository prontuarioRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           TutorRepository tutorRepository,
                           VeterinarioRepository veterinarioRepository,
                           ClinicaRepository clinicaRepository,
                           PetRepository petRepository,
                           AutorizacaoAcessoPetRepository autorizacaoRepository,
                           VeterinarioClinicaRepository vcRepository,
                           NotificacaoRepository notificacaoRepository,
                           ProntuarioRepository prontuarioRepository) {
        this.consultaRepository = consultaRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.petRepository = petRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.vcRepository = vcRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.prontuarioRepository = prontuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Consulta> listConsultasByConta(UUID idConta) {
        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return consultaRepository.findByPetTutorIdTutor(tutorOpt.get().getIdTutor());
        }

        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return consultaRepository.findByVeterinarioIdVeterinario(vetOpt.get().getIdVeterinario());
        }

        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);
        if (clinicaOpt.isPresent()) {
            return consultaRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        }

        return Collections.emptyList();
    }

    @Transactional
    public Consulta createConsulta(Consulta consulta, UUID idConta) {
        consulta.setIdConsulta(null);

        // 1. Validar tutor autenticado
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new AccessDeniedException("Apenas contas de tutores podem solicitar agendamento de consultas."));

        // 2. Validar pet e sua propriedade
        if (consulta.getPet() == null || consulta.getPet().getIdPet() == null) {
            throw new BusinessException("O pet é obrigatório para o agendamento.");
        }

        Pet pet = petRepository.findById(consulta.getPet().getIdPet())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com ID: " + consulta.getPet().getIdPet()));

        if (pet.getTutor() == null || !pet.getTutor().getIdTutor().equals(tutor.getIdTutor())) {
            throw new AccessDeniedException("Acesso negado: o pet informado não pertence ao tutor autenticado.");
        }

        if (Boolean.FALSE.equals(pet.getAtivo())) {
            throw new BusinessException("Não é possível agendar consultas para um pet inativo.");
        }
        consulta.setPet(pet);

        // 3. Validar data futura
        if (consulta.getDataHora() == null) {
            throw new BusinessException("A data e hora da consulta são obrigatórias.");
        }
        if (consulta.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data da consulta não pode ser no passado.");
        }

        // 4. Validar veterinário
        if (consulta.getVeterinario() == null || consulta.getVeterinario().getIdVeterinario() == null) {
            throw new BusinessException("O médico veterinário é obrigatório.");
        }
        Veterinario vet = veterinarioRepository.findById(consulta.getVeterinario().getIdVeterinario())
                .orElseThrow(() -> new ResourceNotFoundException("Médico veterinário não encontrado."));
        consulta.setVeterinario(vet);

        // 5. Validar clínica e vínculo com o veterinário
        Clinica clinica = null;
        if (consulta.getClinica() != null && consulta.getClinica().getIdClinica() != null) {
            clinica = clinicaRepository.findById(consulta.getClinica().getIdClinica())
                    .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada."));

            Optional<VeterinarioClinica> vinculo = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(vet.getIdVeterinario(), clinica.getIdClinica());
            if (vinculo.isEmpty() || !"ATIVO".equalsIgnoreCase(vinculo.get().getStatusVinculo())) {
                throw new BusinessException("O médico veterinário selecionado não possui vínculo ativo com a clínica informada.");
            }
        } else {
            List<VeterinarioClinica> vinculos = vcRepository.findByVeterinarioIdVeterinarioAndStatusVinculo(vet.getIdVeterinario(), "ATIVO");
            if (vinculos.isEmpty()) {
                throw new BusinessException("O veterinário selecionado não possui vínculo ativo com nenhuma clínica.");
            }
            clinica = vinculos.get(0).getClinica();
        }

        if (Boolean.FALSE.equals(clinica.getAtiva())) {
            throw new BusinessException("A clínica selecionada encontra-se inativa.");
        }
        consulta.setClinica(clinica);

        // 6. Validar conflito de agenda do veterinário
        List<Consulta> conflitos = consultaRepository.findByVeterinarioIdVeterinarioAndDataHora(vet.getIdVeterinario(), consulta.getDataHora());
        boolean ocupado = conflitos.stream().anyMatch(c -> !"CANCELADA".equalsIgnoreCase(c.getStatus()));
        if (ocupado) {
            throw new BusinessException("O médico veterinário já possui uma consulta agendada para este horário.");
        }

        // 7. Status inicial controlado estritamente pelo servidor
        consulta.setStatus("AGENDADO");
        if (consulta.getModalidade() == null || consulta.getModalidade().isBlank()) {
            consulta.setModalidade("PRESENCIAL");
        }

        Consulta salvo = consultaRepository.save(consulta);

        // 8. Gerar automaticamente AutorizacaoAcessoPet vinculada ao tutor real do pet (LGPD)
        Optional<AutorizacaoAcessoPet> authExistente = autorizacaoRepository
                .findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(pet.getIdPet(), vet.getIdVeterinario(), "ATIVA");

        if (authExistente.isEmpty()) {
            AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
            auth.setPet(pet);
            auth.setVeterinario(vet);
            auth.setClinica(clinica);
            auth.setContaAutorizador(tutor.getContaAcesso());
            auth.setDataAutorizacao(LocalDateTime.now());
            auth.setStatus("ATIVA");
            autorizacaoRepository.save(auth);
        }

        // 9. Notificações
        String vetName = vet.getNome();
        String clinicaName = clinica != null ? " na unidade " + clinica.getNomeFantasia() : "";
        Notificacao notif = new Notificacao();
        notif.setContaAcesso(tutor.getContaAcesso());
        notif.setMensagem("Sua consulta com " + vetName + clinicaName + " foi agendada com sucesso!");
        notificacaoRepository.save(notif);

        if (vet.getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(vet.getContaAcesso());
            notifVet.setMensagem("Nova consulta agendada para o paciente " + pet.getNome() + " em " + consulta.getDataHora() + ".");
            notificacaoRepository.save(notifVet);
        }

        return salvo;
    }

    @Transactional
    public void cancelarConsulta(UUID idConsulta, UUID idConta) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));

        if ("CONCLUIDA".equalsIgnoreCase(consulta.getStatus())) {
            throw new BusinessException("Não é possível cancelar uma consulta já concluída.");
        }

        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);

        boolean autorizado = false;
        if (tutorOpt.isPresent() && consulta.getPet() != null && consulta.getPet().getTutor() != null) {
            autorizado = consulta.getPet().getTutor().getIdTutor().equals(tutorOpt.get().getIdTutor());
        } else if (vetOpt.isPresent() && consulta.getVeterinario() != null) {
            autorizado = consulta.getVeterinario().getIdVeterinario().equals(vetOpt.get().getIdVeterinario());
        } else if (clinicaOpt.isPresent() && consulta.getClinica() != null) {
            autorizado = consulta.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica());
        }

        if (!autorizado) {
            throw new AccessDeniedException("Acesso negado: você não tem permissão para cancelar esta consulta.");
        }

        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);

        String petNome = consulta.getPet() != null ? consulta.getPet().getNome() : "Pet";

        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notifTutor = new Notificacao();
            notifTutor.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            notifTutor.setMensagem("A consulta do pet " + petNome + " foi cancelada com sucesso.");
            notificacaoRepository.save(notifTutor);
        }

        if (consulta.getVeterinario() != null && consulta.getVeterinario().getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(consulta.getVeterinario().getContaAcesso());
            notifVet.setMensagem("A consulta do pet " + petNome + " foi cancelada.");
            notificacaoRepository.save(notifVet);
        }
    }

    @Transactional
    public void completeConsulta(UUID idConsulta, UUID idConta, String clinicalNotes) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));

        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);

        boolean autorizado = false;
        if (vetOpt.isPresent() && consulta.getVeterinario() != null) {
            autorizado = consulta.getVeterinario().getIdVeterinario().equals(vetOpt.get().getIdVeterinario());
        } else if (clinicaOpt.isPresent() && consulta.getClinica() != null) {
            autorizado = consulta.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica());
        }

        if (!autorizado) {
            throw new AccessDeniedException("Acesso negado: apenas o médico veterinário ou a clínica responsável podem concluir o atendimento.");
        }

        consulta.setStatus("CONCLUIDA");
        consultaRepository.save(consulta);

        // Persistência real do prontuário clínico
        if (clinicalNotes != null && !clinicalNotes.isBlank()) {
            Prontuario prontuario = prontuarioRepository.findByConsultaIdConsulta(consulta.getIdConsulta())
                    .orElse(new Prontuario());
            prontuario.setConsulta(consulta);
            prontuario.setVeterinario(consulta.getVeterinario());
            prontuario.setNotasClinicas(clinicalNotes);
            prontuario.setDataRegistro(LocalDateTime.now());
            prontuarioRepository.save(prontuario);
        }

        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notif = new Notificacao();
            notif.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            String notas = (clinicalNotes != null && !clinicalNotes.isBlank())
                    ? " Observações clínicas: " + clinicalNotes : "";
            notif.setMensagem("A consulta do pet " + consulta.getPet().getNome() + " foi finalizada com sucesso." + notas);
            notificacaoRepository.save(notif);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Consulta> findById(UUID idConsulta) {
        return consultaRepository.findById(idConsulta);
    }
}

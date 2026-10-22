package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.ModalidadeConsulta;
import com.clyvo.veterinary.models.enums.StatusAutorizacao;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.models.enums.StatusVinculo;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ConsultaService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ConsultaRepository consultaRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PetRepository petRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final VeterinarioClinicaRepository vcRepository;
    private final NotificacaoService notificacaoService;
    private final ProntuarioRepository prontuarioRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           TutorRepository tutorRepository,
                           VeterinarioRepository veterinarioRepository,
                           ClinicaRepository clinicaRepository,
                           PetRepository petRepository,
                           AutorizacaoAcessoPetRepository autorizacaoRepository,
                           VeterinarioClinicaRepository vcRepository,
                           NotificacaoService notificacaoService,
                           ProntuarioRepository prontuarioRepository) {
        this.consultaRepository = consultaRepository;
        this.tutorRepository = tutorRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.petRepository = petRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.vcRepository = vcRepository;
        this.notificacaoService = notificacaoService;
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

        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new AccessDeniedException("Apenas contas de tutores podem solicitar agendamento de consultas."));

        Pet pet = validarPetDoTutor(consulta.getPet(), tutor);
        consulta.setPet(pet);

        validarDataFutura(consulta.getDataHora());

        Veterinario vet = buscarVeterinario(consulta.getVeterinario());
        consulta.setVeterinario(vet);

        Clinica clinica = resolverClinicaDoVeterinario(consulta.getClinica(), vet);
        consulta.setClinica(clinica);

        validarAgendaLivre(vet, consulta.getDataHora());

        // Status inicial controlado estritamente pelo servidor
        consulta.setStatus(StatusConsulta.AGENDADO.name());
        if (consulta.getModalidade() == null || consulta.getModalidade().isBlank()) {
            consulta.setModalidade(ModalidadeConsulta.PRESENCIAL.name());
        }

        Consulta salvo = consultaRepository.save(consulta);

        garantirAutorizacaoDeAcesso(pet, vet, clinica, tutor);

        notificacaoService.notificar(tutor.getContaAcesso(),
                "Sua consulta com " + vet.getNome() + " na unidade " + clinica.getNomeFantasia() + " foi agendada com sucesso!");
        notificacaoService.notificar(vet.getContaAcesso(),
                "Nova consulta agendada para o paciente " + pet.getNome() + " em " + consulta.getDataHora().format(DATA_HORA) + ".");

        return salvo;
    }

    @Transactional
    public void cancelarConsulta(UUID idConsulta, UUID idConta) {
        Consulta consulta = buscarConsulta(idConsulta);

        if (StatusConsulta.CONCLUIDA.matches(consulta.getStatus())) {
            throw new BusinessException("Não é possível cancelar uma consulta já concluída.");
        }

        boolean autorizado = isTutorDaConsulta(consulta, idConta)
                || isVeterinarioDaConsulta(consulta, idConta)
                || isClinicaDaConsulta(consulta, idConta);
        if (!autorizado) {
            throw new AccessDeniedException("Acesso negado: você não tem permissão para cancelar esta consulta.");
        }

        consulta.setStatus(StatusConsulta.CANCELADA.name());
        consultaRepository.save(consulta);

        String petNome = consulta.getPet() != null ? consulta.getPet().getNome() : "Pet";
        notificacaoService.notificar(contaDoTutor(consulta), "A consulta do pet " + petNome + " foi cancelada com sucesso.");
        if (consulta.getVeterinario() != null) {
            notificacaoService.notificar(consulta.getVeterinario().getContaAcesso(), "A consulta do pet " + petNome + " foi cancelada.");
        }
    }

    @Transactional
    public void completeConsulta(UUID idConsulta, UUID idConta, String clinicalNotes) {
        Consulta consulta = buscarConsulta(idConsulta);

        if (!isVeterinarioDaConsulta(consulta, idConta) && !isClinicaDaConsulta(consulta, idConta)) {
            throw new AccessDeniedException("Acesso negado: apenas o médico veterinário ou a clínica responsável podem concluir o atendimento.");
        }
        if (!StatusConsulta.AGENDADO.matches(consulta.getStatus())) {
            throw new BusinessException("Apenas consultas agendadas podem ser concluídas.");
        }

        consulta.setStatus(StatusConsulta.CONCLUIDA.name());
        consultaRepository.save(consulta);

        if (clinicalNotes != null && !clinicalNotes.isBlank()) {
            Prontuario prontuario = prontuarioRepository.findByConsultaIdConsulta(consulta.getIdConsulta())
                    .orElse(new Prontuario());
            prontuario.setConsulta(consulta);
            prontuario.setVeterinario(consulta.getVeterinario());
            prontuario.setNotasClinicas(clinicalNotes);
            prontuario.setDataRegistro(LocalDateTime.now());
            prontuarioRepository.save(prontuario);
        }

        // Dados clínicos ficam no prontuário (acesso controlado); a notificação apenas avisa o tutor (LGPD).
        if (consulta.getPet() != null) {
            notificacaoService.notificar(contaDoTutor(consulta),
                    "A consulta do pet " + consulta.getPet().getNome() + " foi finalizada. O prontuário já está disponível no seu painel.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<Consulta> findById(UUID idConsulta) {
        return consultaRepository.findById(idConsulta);
    }

    @Transactional(readOnly = true)
    public List<Prontuario> listProntuariosByConta(UUID idConta) {
        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return prontuarioRepository.findByConsultaPetTutorIdTutorOrderByDataRegistroDesc(tutorOpt.get().getIdTutor());
        }
        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return prontuarioRepository.findByVeterinarioIdVeterinarioOrderByDataRegistroDesc(vetOpt.get().getIdVeterinario());
        }
        return Collections.emptyList();
    }

    // ------------------------------------------------------------------
    // Regras de validação do agendamento
    // ------------------------------------------------------------------

    private Pet validarPetDoTutor(Pet petInformado, Tutor tutor) {
        if (petInformado == null || petInformado.getIdPet() == null) {
            throw new BusinessException("O pet é obrigatório para o agendamento.");
        }
        Pet pet = petRepository.findById(petInformado.getIdPet())
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com ID: " + petInformado.getIdPet()));

        if (pet.getTutor() == null || !pet.getTutor().getIdTutor().equals(tutor.getIdTutor())) {
            throw new AccessDeniedException("Acesso negado: o pet informado não pertence ao tutor autenticado.");
        }
        if (Boolean.FALSE.equals(pet.getAtivo())) {
            throw new BusinessException("Não é possível agendar consultas para um pet inativo.");
        }
        return pet;
    }

    private void validarDataFutura(LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new BusinessException("A data e hora da consulta são obrigatórias.");
        }
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data da consulta não pode ser no passado.");
        }
    }

    private Veterinario buscarVeterinario(Veterinario vetInformado) {
        if (vetInformado == null || vetInformado.getIdVeterinario() == null) {
            throw new BusinessException("O médico veterinário é obrigatório.");
        }
        return veterinarioRepository.findById(vetInformado.getIdVeterinario())
                .orElseThrow(() -> new ResourceNotFoundException("Médico veterinário não encontrado."));
    }

    /** Usa a clínica informada (validando o vínculo) ou, se ausente, a primeira clínica ativa do veterinário. */
    private Clinica resolverClinicaDoVeterinario(Clinica clinicaInformada, Veterinario vet) {
        Clinica clinica;
        if (clinicaInformada != null && clinicaInformada.getIdClinica() != null) {
            clinica = clinicaRepository.findById(clinicaInformada.getIdClinica())
                    .orElseThrow(() -> new ResourceNotFoundException("Clínica não encontrada."));

            boolean vinculoAtivo = vcRepository
                    .findByVeterinarioIdVeterinarioAndClinicaIdClinica(vet.getIdVeterinario(), clinica.getIdClinica())
                    .filter(v -> StatusVinculo.ATIVO.matches(v.getStatusVinculo()))
                    .isPresent();
            if (!vinculoAtivo) {
                throw new BusinessException("O médico veterinário selecionado não possui vínculo ativo com a clínica informada.");
            }
        } else {
            clinica = vcRepository.findByVeterinarioIdVeterinarioAndStatusVinculo(vet.getIdVeterinario(), StatusVinculo.ATIVO.name())
                    .stream()
                    .findFirst()
                    .map(VeterinarioClinica::getClinica)
                    .orElseThrow(() -> new BusinessException("O veterinário selecionado não possui vínculo ativo com nenhuma clínica."));
        }

        if (Boolean.FALSE.equals(clinica.getAtiva())) {
            throw new BusinessException("A clínica selecionada encontra-se inativa.");
        }
        return clinica;
    }

    private void validarAgendaLivre(Veterinario vet, LocalDateTime dataHora) {
        boolean ocupado = consultaRepository.findByVeterinarioIdVeterinarioAndDataHora(vet.getIdVeterinario(), dataHora)
                .stream()
                .anyMatch(c -> !StatusConsulta.CANCELADA.matches(c.getStatus()));
        if (ocupado) {
            throw new BusinessException("O médico veterinário já possui uma consulta agendada para este horário.");
        }
    }

    /** O agendamento implica consentimento do tutor para o veterinário acessar o prontuário do pet (LGPD). */
    private void garantirAutorizacaoDeAcesso(Pet pet, Veterinario vet, Clinica clinica, Tutor tutor) {
        boolean jaAutorizado = autorizacaoRepository
                .findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(pet.getIdPet(), vet.getIdVeterinario(), StatusAutorizacao.ATIVA.name())
                .isPresent();
        if (jaAutorizado) {
            return;
        }
        AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
        auth.setPet(pet);
        auth.setVeterinario(vet);
        auth.setClinica(clinica);
        auth.setContaAutorizador(tutor.getContaAcesso());
        auth.setDataAutorizacao(LocalDateTime.now());
        auth.setStatus(StatusAutorizacao.ATIVA.name());
        autorizacaoRepository.save(auth);
    }

    // ------------------------------------------------------------------
    // Verificações de titularidade
    // ------------------------------------------------------------------

    private Consulta buscarConsulta(UUID idConsulta) {
        return consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));
    }

    private boolean isTutorDaConsulta(Consulta consulta, UUID idConta) {
        return consulta.getPet() != null && consulta.getPet().getTutor() != null
                && tutorRepository.findByContaAcessoIdConta(idConta)
                    .map(t -> t.getIdTutor().equals(consulta.getPet().getTutor().getIdTutor()))
                    .orElse(false);
    }

    private boolean isVeterinarioDaConsulta(Consulta consulta, UUID idConta) {
        return consulta.getVeterinario() != null
                && veterinarioRepository.findByContaAcessoIdConta(idConta)
                    .map(v -> v.getIdVeterinario().equals(consulta.getVeterinario().getIdVeterinario()))
                    .orElse(false);
    }

    private boolean isClinicaDaConsulta(Consulta consulta, UUID idConta) {
        return consulta.getClinica() != null
                && clinicaRepository.findByContaAcessoIdConta(idConta)
                    .map(c -> c.getIdClinica().equals(consulta.getClinica().getIdClinica()))
                    .orElse(false);
    }

    private static ContaAcesso contaDoTutor(Consulta consulta) {
        return (consulta.getPet() != null && consulta.getPet().getTutor() != null)
                ? consulta.getPet().getTutor().getContaAcesso()
                : null;
    }
}

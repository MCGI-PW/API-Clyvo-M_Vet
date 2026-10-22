package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.models.enums.StatusVinculo;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final VeterinarioClinicaRepository vcRepository;
    private final VeterinarioRepository vetRepository;
    private final ConsultaRepository consultaRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final NotificacaoService notificacaoService;

    public ClinicaService(ClinicaRepository clinicaRepository,
                          VeterinarioClinicaRepository vcRepository,
                          VeterinarioRepository vetRepository,
                          ConsultaRepository consultaRepository,
                          AutorizacaoAcessoPetRepository autorizacaoRepository,
                          NotificacaoService notificacaoService) {
        this.clinicaRepository = clinicaRepository;
        this.vcRepository = vcRepository;
        this.vetRepository = vetRepository;
        this.consultaRepository = consultaRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<Clinica> listClinicasAtivas() {
        return clinicaRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getAtiva()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Clinica> findClinicaByConta(UUID idConta) {
        return clinicaRepository.findByContaAcessoIdConta(idConta);
    }

    /** Clínica da conta autenticada; contas de outros perfis recebem 403. */
    @Transactional(readOnly = true)
    public Clinica getClinicaByConta(UUID idConta) {
        return clinicaRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new AccessDeniedException("Acesso restrito a contas do tipo Clínica."));
    }

    @Transactional(readOnly = true)
    public List<Veterinario> listVeterinariosDaClinica(UUID idClinica) {
        return vcRepository.findByClinicaIdClinicaAndStatusVinculo(idClinica, StatusVinculo.ATIVO.name()).stream()
                .map(VeterinarioClinica::getVeterinario)
                .toList();
    }

    /** Veterinários que ainda não têm vínculo ativo com a clínica (candidatos a vínculo). */
    @Transactional(readOnly = true)
    public List<Veterinario> listVeterinariosDisponiveis(UUID idClinica) {
        Set<UUID> equipe = new HashSet<>();
        listVeterinariosDaClinica(idClinica).forEach(v -> equipe.add(v.getIdVeterinario()));
        return vetRepository.findAll().stream()
                .filter(v -> !equipe.contains(v.getIdVeterinario()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VeterinarioClinica> listMeusVeterinarios(UUID idClinica) {
        return vcRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional
    public VeterinarioClinica vincularVeterinario(Clinica clinica, UUID idVet) {
        Veterinario vet = vetRepository.findById(idVet)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário não encontrado."));

        VeterinarioClinica vc = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(idVet, clinica.getIdClinica())
                .orElseGet(() -> {
                    VeterinarioClinica novo = new VeterinarioClinica();
                    novo.setClinica(clinica);
                    novo.setVeterinario(vet);
                    novo.setDataInicio(LocalDate.now());
                    return novo;
                });
        vc.setStatusVinculo(StatusVinculo.ATIVO.name());
        vc.setDataFim(null);
        VeterinarioClinica salvo = vcRepository.save(vc);

        notificacaoService.notificar(vet.getContaAcesso(),
                "Você foi vinculado como médico veterinário na unidade " + clinica.getNomeFantasia() + ".");
        return salvo;
    }

    @Transactional
    public void desvincularVeterinario(UUID idClinica, UUID idVinculo) {
        VeterinarioClinica vc = vcRepository.findById(idVinculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado."));

        if (!vc.getClinica().getIdClinica().equals(idClinica)) {
            throw new AccessDeniedException("Acesso negado: vínculo não pertence a esta unidade.");
        }

        vc.setStatusVinculo(StatusVinculo.INATIVO.name());
        vc.setDataFim(LocalDate.now());
        vcRepository.save(vc);
    }

    @Transactional(readOnly = true)
    public List<Consulta> listConsultasDaClinica(UUID idClinica) {
        return consultaRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional(readOnly = true)
    public List<AutorizacaoAcessoPet> listAutorizacoesDaClinica(UUID idClinica) {
        return autorizacaoRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional
    public void transferirAutorizacao(Clinica clinica, UUID idAutorizacao, UUID idNovoVet) {
        AutorizacaoAcessoPet auth = autorizacaoRepository.findById(idAutorizacao)
                .orElseThrow(() -> new ResourceNotFoundException("Autorização não encontrada."));

        if (auth.getClinica() == null || !auth.getClinica().getIdClinica().equals(clinica.getIdClinica())) {
            throw new AccessDeniedException("Autorização não pertence a esta clínica.");
        }

        Veterinario novoVet = vetRepository.findById(idNovoVet)
                .orElseThrow(() -> new ResourceNotFoundException("Novo veterinário não encontrado."));

        boolean vinculoAtivo = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(idNovoVet, clinica.getIdClinica())
                .filter(v -> StatusVinculo.ATIVO.matches(v.getStatusVinculo()))
                .isPresent();
        if (!vinculoAtivo) {
            throw new BusinessException("O médico veterinário indicado não possui vínculo ativo nesta unidade.");
        }

        Veterinario vetAnterior = auth.getVeterinario();
        auth.setVeterinario(novoVet);
        autorizacaoRepository.save(auth);

        // Consultas ainda agendadas com o médico anterior nesta unidade passam para o novo responsável
        consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> StatusConsulta.AGENDADO.matches(c.getStatus()))
                .filter(c -> c.getClinica() != null && c.getClinica().getIdClinica().equals(clinica.getIdClinica()))
                .filter(c -> vetAnterior != null && c.getVeterinario() != null
                        && c.getVeterinario().getIdVeterinario().equals(vetAnterior.getIdVeterinario()))
                .forEach(c -> {
                    c.setVeterinario(novoVet);
                    consultaRepository.save(c);
                });

        Pet pet = auth.getPet();
        if (pet.getTutor() != null) {
            notificacaoService.notificar(pet.getTutor().getContaAcesso(),
                    "A clínica " + clinica.getNomeFantasia() + " transferiu o atendimento do pet "
                            + pet.getNome() + " para Dr(a). " + novoVet.getNome() + ".");
        }
        notificacaoService.notificar(novoVet.getContaAcesso(),
                "O paciente " + pet.getNome() + " foi transferido para seu acompanhamento na clínica " + clinica.getNomeFantasia() + ".");
    }

    @Transactional(readOnly = true)
    public List<Pet> listPacientesDaClinica(UUID idClinica) {
        Set<UUID> seenPetIds = new HashSet<>();
        return consultaRepository.findByClinicaIdClinica(idClinica).stream()
                .map(Consulta::getPet)
                .filter(Objects::nonNull)
                .filter(pet -> seenPetIds.add(pet.getIdPet()))
                .toList();
    }
}

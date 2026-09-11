package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final VeterinarioClinicaRepository vcRepository;
    private final VeterinarioRepository vetRepository;
    private final ConsultaRepository consultaRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public ClinicaService(ClinicaRepository clinicaRepository,
                          VeterinarioClinicaRepository vcRepository,
                          VeterinarioRepository vetRepository,
                          ConsultaRepository consultaRepository,
                          AutorizacaoAcessoPetRepository autorizacaoRepository,
                          NotificacaoRepository notificacaoRepository) {
        this.clinicaRepository = clinicaRepository;
        this.vcRepository = vcRepository;
        this.vetRepository = vetRepository;
        this.consultaRepository = consultaRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Clinica> listClinicasAtivas() {
        return clinicaRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getAtiva()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Clinica> findClinicaByConta(UUID idConta) {
        return clinicaRepository.findByContaAcessoIdConta(idConta);
    }

    @Transactional(readOnly = true)
    public List<Veterinario> listVeterinariosDaClinica(UUID idClinica) {
        List<VeterinarioClinica> vinculos = vcRepository.findByClinicaIdClinicaAndStatusVinculo(idClinica, "ATIVO");
        return vinculos.stream()
                .map(VeterinarioClinica::getVeterinario)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VeterinarioClinica> listMeusVeterinarios(UUID idClinica) {
        return vcRepository.findByClinicaIdClinica(idClinica);
    }

    @Transactional
    public VeterinarioClinica vincularVeterinario(Clinica clinica, UUID idVet) {
        Veterinario vet = vetRepository.findById(idVet)
                .orElseThrow(() -> new RuntimeException("Veterinário não encontrado."));

        Optional<VeterinarioClinica> vinculoExistente = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(idVet, clinica.getIdClinica());
        VeterinarioClinica vc;
        if (vinculoExistente.isPresent()) {
            vc = vinculoExistente.get();
            vc.setStatusVinculo("ATIVO");
            vc.setDataFim(null);
        } else {
            vc = new VeterinarioClinica();
            vc.setClinica(clinica);
            vc.setVeterinario(vet);
            vc.setDataInicio(LocalDate.now());
            vc.setStatusVinculo("ATIVO");
        }
        VeterinarioClinica salvo = vcRepository.save(vc);

        if (vet.getContaAcesso() != null) {
            Notificacao notif = new Notificacao();
            notif.setContaAcesso(vet.getContaAcesso());
            notif.setMensagem("Você foi vinculado como médico veterinário na unidade " + clinica.getNomeFantasia() + ".");
            notificacaoRepository.save(notif);
        }

        return salvo;
    }

    @Transactional
    public VeterinarioClinica vincularVeterinario(UUID idClinica, UUID idVet) {
        Clinica clinica = clinicaRepository.findById(idClinica)
                .orElseGet(() -> clinicaRepository.findByContaAcessoIdConta(idClinica)
                        .orElseThrow(() -> new RuntimeException("Clínica não encontrada.")));
        return vincularVeterinario(clinica, idVet);
    }

    @Transactional
    public void desvincularVeterinario(UUID idClinica, UUID idVinculo) {
        VeterinarioClinica vc = vcRepository.findById(idVinculo)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado."));

        if (!vc.getClinica().getIdClinica().equals(idClinica)) {
            throw new RuntimeException("Acesso negado: vínculo não pertence a esta unidade.");
        }

        vc.setStatusVinculo("INATIVO");
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
                .orElseThrow(() -> new RuntimeException("Autorização não encontrada."));

        if (auth.getClinica() == null || !auth.getClinica().getIdClinica().equals(clinica.getIdClinica())) {
            throw new RuntimeException("Autorização não pertence a esta clínica.");
        }

        Veterinario novoVet = vetRepository.findById(idNovoVet)
                .orElseThrow(() -> new RuntimeException("Novo veterinário não encontrado."));

        Optional<VeterinarioClinica> vinculoNovo = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(idNovoVet, clinica.getIdClinica());
        if (vinculoNovo.isEmpty() || !"ATIVO".equalsIgnoreCase(vinculoNovo.get().getStatusVinculo())) {
            throw new RuntimeException("O médico veterinário indicado não possui vínculo ativo nesta unidade.");
        }

        Veterinario vetAnterior = auth.getVeterinario();
        auth.setVeterinario(novoVet);
        autorizacaoRepository.save(auth);

        // Atualizar consultas agendadas associadas
        List<Consulta> consultas = consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> "AGENDADO".equalsIgnoreCase(c.getStatus()) &&
                        c.getClinica() != null &&
                        c.getClinica().getIdClinica().equals(clinica.getIdClinica()) &&
                        c.getVeterinario().getIdVeterinario().equals(vetAnterior.getIdVeterinario()))
                .collect(Collectors.toList());

        for (Consulta c : consultas) {
            c.setVeterinario(novoVet);
            consultaRepository.save(c);
        }

        // Notificar Tutor
        if (auth.getPet() != null && auth.getPet().getTutor() != null && auth.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notifTutor = new Notificacao();
            notifTutor.setContaAcesso(auth.getPet().getTutor().getContaAcesso());
            notifTutor.setMensagem("A clínica " + clinica.getNomeFantasia() + " transferiu o atendimento do pet "
                    + auth.getPet().getNome() + " para Dr(a). " + novoVet.getNome() + ".");
            notificacaoRepository.save(notifTutor);
        }

        // Notificar Novo Veterinário
        if (novoVet.getContaAcesso() != null) {
            Notificacao notifNovoVet = new Notificacao();
            notifNovoVet.setContaAcesso(novoVet.getContaAcesso());
            notifNovoVet.setMensagem("O paciente " + auth.getPet().getNome() + " foi transferido para seu acompanhamento na clínica " + clinica.getNomeFantasia() + ".");
            notificacaoRepository.save(notifNovoVet);
        }
    }

    @Transactional
    public void transferirAutorizacao(UUID idClinica, UUID idAutorizacao, UUID idNovoVet) {
        Clinica clinica = clinicaRepository.findById(idClinica)
                .orElseGet(() -> clinicaRepository.findByContaAcessoIdConta(idClinica)
                        .orElseThrow(() -> new RuntimeException("Clínica não encontrada.")));
        transferirAutorizacao(clinica, idAutorizacao, idNovoVet);
    }

    @Transactional(readOnly = true)
    public List<Pet> listPacientesDaClinica(UUID idClinica) {
        List<Consulta> consultas = consultaRepository.findByClinicaIdClinica(idClinica);
        Set<UUID> seenPetIds = new HashSet<>();
        List<Pet> pets = new ArrayList<>();
        for (Consulta c : consultas) {
            if (c.getPet() != null && seenPetIds.add(c.getPet().getIdPet())) {
                pets.add(c.getPet());
            }
        }
        return pets;
    }
}

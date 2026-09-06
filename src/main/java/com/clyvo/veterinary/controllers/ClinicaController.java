package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    private final ClinicaRepository clinicaRepository;
    private final VeterinarioClinicaRepository vcRepository;
    private final VeterinarioRepository vetRepository;
    private final ConsultaRepository consultaRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final NotificacaoRepository notificacaoRepository;

    public ClinicaController(ClinicaRepository clinicaRepository,
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

    private Optional<Clinica> getLoggedClinica() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        return clinicaRepository.findByContaAcessoIdConta(idConta);
    }

    @GetMapping
    public ResponseEntity<List<Clinica>> listClinicasAtivas() {
        List<Clinica> clinicas = clinicaRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getAtiva()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(clinicas);
    }

    @GetMapping("/minha")
    public ResponseEntity<?> getMinhaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas contas do tipo CLÍNICA podem acessar este recurso."));
        }
        return ResponseEntity.ok(clinicaOpt.get());
    }

    @GetMapping("/{idClinica}/veterinarios")
    public ResponseEntity<List<Veterinario>> listVeterinariosDaClinica(@PathVariable UUID idClinica) {
        List<VeterinarioClinica> vinculos = vcRepository.findByClinicaIdClinicaAndStatusVinculo(idClinica, "ATIVO");
        List<Veterinario> vets = vinculos.stream()
                .map(VeterinarioClinica::getVeterinario)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vets);
    }

    @GetMapping("/meus-veterinarios")
    public ResponseEntity<?> listMeusVeterinarios() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        List<VeterinarioClinica> vinculos = vcRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        return ResponseEntity.ok(vinculos);
    }

    @PostMapping("/veterinarios/vincular")
    public ResponseEntity<?> vincularVeterinario(@RequestBody Map<String, String> request) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        String idVetStr = request.get("idVeterinario");
        if (idVetStr == null || idVetStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do veterinário é obrigatório."));
        }

        UUID idVet = UUID.fromString(idVetStr);
        Veterinario vet = vetRepository.findById(idVet).orElse(null);
        if (vet == null) {
            return ResponseEntity.notFound().build();
        }

        Clinica clinica = clinicaOpt.get();
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
        vcRepository.save(vc);

        // Notificar veterinário
        if (vet.getContaAcesso() != null) {
            Notificacao notif = new Notificacao();
            notif.setContaAcesso(vet.getContaAcesso());
            notif.setMensagem("Você foi vinculado como médico veterinário na unidade " + clinica.getNomeFantasia() + ".");
            notificacaoRepository.save(notif);
        }

        return ResponseEntity.ok(Map.of("message", "Veterinário vinculado com sucesso à clínica."));
    }

    @PutMapping("/veterinarios/{idVinculo}/desvincular")
    public ResponseEntity<?> desvincularVeterinario(@PathVariable UUID idVinculo) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        VeterinarioClinica vc = vcRepository.findById(idVinculo).orElse(null);
        if (vc == null || !vc.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Vínculo não encontrado na sua clínica."));
        }

        vc.setStatusVinculo("INATIVO");
        vc.setDataFim(LocalDate.now());
        vcRepository.save(vc);

        return ResponseEntity.ok(Map.of("message", "Vínculo desativado com sucesso."));
    }

    @GetMapping("/consultas")
    public ResponseEntity<?> listConsultasDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        List<Consulta> consultas = consultaRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/autorizacoes")
    public ResponseEntity<?> listAutorizacoesDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        List<AutorizacaoAcessoPet> auths = autorizacaoRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        return ResponseEntity.ok(auths);
    }

    @PutMapping("/autorizacoes/{idAutorizacao}/transferir")
    public ResponseEntity<?> transferirAutorizacao(@PathVariable UUID idAutorizacao,
                                                   @RequestBody Map<String, String> body) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        AutorizacaoAcessoPet auth = autorizacaoRepository.findById(idAutorizacao).orElse(null);
        if (auth == null || auth.getClinica() == null || !auth.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Autorização não encontrada na sua clínica."));
        }

        String idNovoVetStr = body.get("idNovoVeterinario");
        if (idNovoVetStr == null || idNovoVetStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do novo veterinário é obrigatório."));
        }

        UUID idNovoVet = UUID.fromString(idNovoVetStr);
        Veterinario novoVet = vetRepository.findById(idNovoVet).orElse(null);
        if (novoVet == null) {
            return ResponseEntity.notFound().build();
        }

        // Valida se novo veterinario é ativo nesta clinica
        Optional<VeterinarioClinica> vinculoNovo = vcRepository.findByVeterinarioIdVeterinarioAndClinicaIdClinica(idNovoVet, clinicaOpt.get().getIdClinica());
        if (vinculoNovo.isEmpty() || !"ATIVO".equalsIgnoreCase(vinculoNovo.get().getStatusVinculo())) {
            return ResponseEntity.badRequest().body(Map.of("error", "O médico veterinário indicado não possui vínculo ativo nesta unidade."));
        }

        Veterinario vetAnterior = auth.getVeterinario();
        auth.setVeterinario(novoVet);
        autorizacaoRepository.save(auth);

        // Atualizar consultas agendadas associadas
        List<Consulta> consultas = consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> "AGENDADO".equalsIgnoreCase(c.getStatus()) &&
                        c.getClinica() != null &&
                        c.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica()) &&
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
            notifTutor.setMensagem("A clínica " + clinicaOpt.get().getNomeFantasia() + " transferiu o atendimento do pet "
                    + auth.getPet().getNome() + " para Dr(a). " + novoVet.getNome() + ".");
            notificacaoRepository.save(notifTutor);
        }

        // Notificar Novo Veterinário
        if (novoVet.getContaAcesso() != null) {
            Notificacao notifNovoVet = new Notificacao();
            notifNovoVet.setContaAcesso(novoVet.getContaAcesso());
            notifNovoVet.setMensagem("O paciente " + auth.getPet().getNome() + " foi transferido para seu acompanhamento na clínica " + clinicaOpt.get().getNomeFantasia() + ".");
            notificacaoRepository.save(notifNovoVet);
        }

        return ResponseEntity.ok(Map.of("message", "Autorização e consultas ativas transferidas com sucesso para Dr(a). " + novoVet.getNome() + "."));
    }

    @GetMapping("/pacientes")
    public ResponseEntity<?> listPacientesDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        List<Consulta> consultas = consultaRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica());
        Set<UUID> seenPetIds = new HashSet<>();
        List<Pet> pets = new ArrayList<>();
        for (Consulta c : consultas) {
            if (c.getPet() != null && seenPetIds.add(c.getPet().getIdPet())) {
                pets.add(c.getPet());
            }
        }
        return ResponseEntity.ok(pets);
    }
}

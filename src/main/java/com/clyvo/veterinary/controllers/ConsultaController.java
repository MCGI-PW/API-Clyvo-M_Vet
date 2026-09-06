package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {
    
    private final ConsultaRepository consultaRepository;
    private final TutorRepository tutorRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PetRepository petRepository;
    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final VeterinarioClinicaRepository vcRepository;
    
    public ConsultaController(ConsultaRepository consultaRepository,
                              TutorRepository tutorRepository,
                              NotificacaoRepository notificacaoRepository,
                              VeterinarioRepository veterinarioRepository,
                              ClinicaRepository clinicaRepository,
                              PetRepository petRepository,
                              AutorizacaoAcessoPetRepository autorizacaoRepository,
                              VeterinarioClinicaRepository vcRepository) {
        this.consultaRepository = consultaRepository;
        this.tutorRepository = tutorRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.petRepository = petRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.vcRepository = vcRepository;
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listConsultas() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        
        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return ResponseEntity.ok(consultaRepository.findByPetTutorIdTutor(tutorOpt.get().getIdTutor()));
        }

        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return ResponseEntity.ok(consultaRepository.findByVeterinarioIdVeterinario(vetOpt.get().getIdVeterinario()));
        }

        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);
        if (clinicaOpt.isPresent()) {
            return ResponseEntity.ok(consultaRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica()));
        }

        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody Consulta consulta) {
        consulta.setIdConsulta(null);

        // Resolver Pet completo
        Pet pet = null;
        if (consulta.getPet() != null && consulta.getPet().getIdPet() != null) {
            pet = petRepository.findById(consulta.getPet().getIdPet()).orElse(null);
            if (pet != null) {
                consulta.setPet(pet);
            }
        }

        // Resolver Veterinário completo
        Veterinario vet = null;
        if (consulta.getVeterinario() != null && consulta.getVeterinario().getIdVeterinario() != null) {
            vet = veterinarioRepository.findById(consulta.getVeterinario().getIdVeterinario()).orElse(null);
            if (vet != null) {
                consulta.setVeterinario(vet);
            }
        }

        // Resolver Clinica
        Clinica clinica = null;
        if (consulta.getClinica() != null && consulta.getClinica().getIdClinica() != null) {
            clinica = clinicaRepository.findById(consulta.getClinica().getIdClinica()).orElse(null);
        } else if (vet != null) {
            // Fallback: busca primeiro vínculo ativo do veterinário com clínica
            List<VeterinarioClinica> vinculos = vcRepository.findByVeterinarioIdVeterinarioAndStatusVinculo(vet.getIdVeterinario(), "ATIVO");
            if (!vinculos.isEmpty()) {
                clinica = vinculos.get(0).getClinica();
            }
        }
        consulta.setClinica(clinica);

        consultaRepository.save(consulta);
        
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);

        // Gerar automaticamente AutorizacaoAcessoPet se não existir ativa
        if (pet != null && vet != null) {
            Optional<AutorizacaoAcessoPet> authExistente = autorizacaoRepository
                    .findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(pet.getIdPet(), vet.getIdVeterinario(), "ATIVA");

            if (authExistente.isEmpty()) {
                AutorizacaoAcessoPet auth = new AutorizacaoAcessoPet();
                auth.setPet(pet);
                auth.setVeterinario(vet);
                auth.setClinica(clinica);

                ContaAcesso autorizador = new ContaAcesso();
                autorizador.setIdConta(idConta);
                auth.setContaAutorizador(autorizador);
                auth.setDataAutorizacao(LocalDateTime.now());
                auth.setStatus("ATIVA");
                autorizacaoRepository.save(auth);
            }
        }

        // Notificar o tutor
        String vetName = vet != null ? vet.getNome() : "nosso time";
        String clinicaName = clinica != null ? " na unidade " + clinica.getNomeFantasia() : "";
        Notificacao notif = new Notificacao();
        ContaAcesso conta = new ContaAcesso();
        conta.setIdConta(idConta);
        notif.setContaAcesso(conta);
        notif.setMensagem("Sua consulta com " + vetName + clinicaName + " foi agendada com sucesso!");
        notificacaoRepository.save(notif);

        // Notificar o veterinário
        if (vet != null && vet.getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(vet.getContaAcesso());
            notifVet.setMensagem("Nova consulta agendada para o paciente " + (pet != null ? pet.getNome() : "Pet") + " em " + consulta.getDataHora() + ".");
            notificacaoRepository.save(notifVet);
        }
        
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarConsulta(@PathVariable UUID id) {
        return executeCancelamento(id);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarConsultaPost(@PathVariable UUID id) {
        return executeCancelamento(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarConsultaDelete(@PathVariable UUID id) {
        return executeCancelamento(id);
    }

    private ResponseEntity<?> executeCancelamento(UUID id) {
        Consulta consulta = consultaRepository.findById(id).orElse(null);
        if (consulta == null) {
            return ResponseEntity.notFound().build();
        }

        if ("CONCLUIDA".equalsIgnoreCase(consulta.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Não é possível cancelar uma consulta já concluída."));
        }

        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);

        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);

        if (tutorOpt.isPresent()) {
            if (consulta.getPet() != null && consulta.getPet().getTutor() != null) {
                if (!consulta.getPet().getTutor().getIdTutor().equals(tutorOpt.get().getIdTutor())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Acesso negado: esta consulta pertence a outro tutor."));
                }
            }
        } else if (vetOpt.isPresent()) {
            if (consulta.getVeterinario() != null) {
                if (!consulta.getVeterinario().getIdVeterinario().equals(vetOpt.get().getIdVeterinario())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Acesso negado: esta consulta pertence a outro veterinário."));
                }
            }
        } else if (clinicaOpt.isPresent()) {
            if (consulta.getClinica() != null) {
                if (!consulta.getClinica().getIdClinica().equals(clinicaOpt.get().getIdClinica())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Acesso negado: esta consulta pertence a outra clínica."));
                }
            }
        }

        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);

        String petNome = consulta.getPet() != null ? consulta.getPet().getNome() : "Pet";
        
        // Notify Tutor
        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notifTutor = new Notificacao();
            notifTutor.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            notifTutor.setMensagem("A consulta do pet " + petNome + " foi cancelada com sucesso.");
            notificacaoRepository.save(notifTutor);
        }

        // Notify Veterinarian
        if (consulta.getVeterinario() != null && consulta.getVeterinario().getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(consulta.getVeterinario().getContaAcesso());
            notifVet.setMensagem("A consulta do pet " + petNome + " foi cancelada.");
            notificacaoRepository.save(notifVet);
        }

        return ResponseEntity.ok(Map.of("message", "Consulta cancelada com sucesso!"));
    }
}

package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {
    
    private final ConsultaRepository consultaRepository;
    private final TutorRepository tutorRepository;
    private final com.clyvo.veterinary.repositories.NotificacaoRepository notificacaoRepository;
    private final com.clyvo.veterinary.repositories.VeterinarioRepository veterinarioRepository;
    
    public ConsultaController(ConsultaRepository consultaRepository, TutorRepository tutorRepository, com.clyvo.veterinary.repositories.NotificacaoRepository notificacaoRepository, com.clyvo.veterinary.repositories.VeterinarioRepository veterinarioRepository) {
        this.consultaRepository = consultaRepository;
        this.tutorRepository = tutorRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listConsultas() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        
        java.util.Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return ResponseEntity.ok(consultaRepository.findByPetTutorIdTutor(tutorOpt.get().getIdTutor()));
        }

        java.util.Optional<com.clyvo.veterinary.models.Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return ResponseEntity.ok(consultaRepository.findByVeterinarioIdVeterinario(vetOpt.get().getIdVeterinario()));
        }

        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody Consulta consulta) {
        consulta.setIdConsulta(null);
        consultaRepository.save(consulta);
        
        // Notify the user
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        com.clyvo.veterinary.models.Veterinario vet = veterinarioRepository.findById(consulta.getVeterinario().getIdVeterinario()).orElse(null);
        String vetName = vet != null ? vet.getNome() : "nosso time";
        
        com.clyvo.veterinary.models.Notificacao notif = new com.clyvo.veterinary.models.Notificacao();
        com.clyvo.veterinary.models.ContaAcesso conta = new com.clyvo.veterinary.models.ContaAcesso();
        conta.setIdConta(UUID.fromString(idContaStr));
        notif.setContaAcesso(conta);
        notif.setMensagem("Sua consulta com " + vetName + " foi agendada com sucesso!");
        notificacaoRepository.save(notif);
        
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
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Não é possível cancelar uma consulta já concluída."));
        }

        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);

        java.util.Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        java.util.Optional<com.clyvo.veterinary.models.Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);

        if (tutorOpt.isPresent()) {
            if (consulta.getPet() != null && consulta.getPet().getTutor() != null) {
                if (!consulta.getPet().getTutor().getIdTutor().equals(tutorOpt.get().getIdTutor())) {
                    return ResponseEntity.status(403).body(java.util.Map.of("error", "Acesso negado: esta consulta pertence a outro tutor."));
                }
            }
        } else if (vetOpt.isPresent()) {
            if (consulta.getVeterinario() != null) {
                if (!consulta.getVeterinario().getIdVeterinario().equals(vetOpt.get().getIdVeterinario())) {
                    return ResponseEntity.status(403).body(java.util.Map.of("error", "Acesso negado: esta consulta pertence a outro veterinário."));
                }
            }
        }

        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);

        String petNome = consulta.getPet() != null ? consulta.getPet().getNome() : "Pet";
        
        // Notify Tutor
        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            com.clyvo.veterinary.models.Notificacao notifTutor = new com.clyvo.veterinary.models.Notificacao();
            notifTutor.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            notifTutor.setMensagem("A consulta do pet " + petNome + " foi cancelada com sucesso.");
            notificacaoRepository.save(notifTutor);
        }

        // Notify Veterinarian
        if (consulta.getVeterinario() != null && consulta.getVeterinario().getContaAcesso() != null) {
            com.clyvo.veterinary.models.Notificacao notifVet = new com.clyvo.veterinary.models.Notificacao();
            notifVet.setContaAcesso(consulta.getVeterinario().getContaAcesso());
            notifVet.setMensagem("A consulta do pet " + petNome + " foi cancelada.");
            notificacaoRepository.save(notifVet);
        }

        return ResponseEntity.ok(java.util.Map.of("message", "Consulta cancelada com sucesso!"));
    }
}

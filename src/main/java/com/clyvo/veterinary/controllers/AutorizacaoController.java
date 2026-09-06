package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/autorizacoes")
public class AutorizacaoController {

    private final AutorizacaoAcessoPetRepository autorizacaoRepository;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;

    public AutorizacaoController(AutorizacaoAcessoPetRepository autorizacaoRepository,
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

    @GetMapping
    public ResponseEntity<List<AutorizacaoAcessoPet>> listAutorizacoes() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);

        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isPresent()) {
            return ResponseEntity.ok(autorizacaoRepository.findByPetTutorIdTutor(tutorOpt.get().getIdTutor()));
        }

        Optional<Veterinario> vetOpt = veterinarioRepository.findByContaAcessoIdConta(idConta);
        if (vetOpt.isPresent()) {
            return ResponseEntity.ok(autorizacaoRepository.findByVeterinarioIdVeterinarioAndStatus(vetOpt.get().getIdVeterinario(), "ATIVA"));
        }

        Optional<Clinica> clinicaOpt = clinicaRepository.findByContaAcessoIdConta(idConta);
        if (clinicaOpt.isPresent()) {
            return ResponseEntity.ok(autorizacaoRepository.findByClinicaIdClinica(clinicaOpt.get().getIdClinica()));
        }

        return ResponseEntity.ok(Collections.emptyList());
    }

    @PutMapping("/{id}/revogar")
    public ResponseEntity<?> revogarAutorizacao(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);

        AutorizacaoAcessoPet auth = autorizacaoRepository.findById(id).orElse(null);
        if (auth == null) {
            return ResponseEntity.notFound().build();
        }

        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isEmpty() || auth.getPet() == null || auth.getPet().getTutor() == null ||
                !auth.getPet().getTutor().getIdTutor().equals(tutorOpt.get().getIdTutor())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas o tutor responsável pelo pet pode revogar esta autorização."));
        }

        String motivo = (body != null && body.containsKey("motivo")) ? body.get("motivo") : "Revogado pelo tutor via painel de controle.";
        auth.setStatus("REVOGADA");
        auth.setMotivoRevogacao(motivo);
        autorizacaoRepository.save(auth);

        // Cancelar consultas agendadas associadas a este veterinario/clinica para o pet
        List<Consulta> consultas = consultaRepository.findByPetIdPet(auth.getPet().getIdPet()).stream()
                .filter(c -> "AGENDADO".equalsIgnoreCase(c.getStatus()) &&
                        c.getVeterinario().getIdVeterinario().equals(auth.getVeterinario().getIdVeterinario()))
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

        // Notificacao Veterinario
        if (auth.getVeterinario().getContaAcesso() != null) {
            Notificacao notifVet = new Notificacao();
            notifVet.setContaAcesso(auth.getVeterinario().getContaAcesso());
            notifVet.setMensagem("A autorização de acesso ao pet " + auth.getPet().getNome() + " foi revogada pelo tutor. Consultas pendentes foram canceladas.");
            notificacaoRepository.save(notifVet);
        }

        return ResponseEntity.ok(Map.of("message", "Autorização revogada com sucesso e consultas ativas canceladas."));
    }
}

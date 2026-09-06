package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.CompleteAppointmentRequest;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Notificacao;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.NotificacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final ConsultaRepository consultaRepository;
    private final NotificacaoRepository notificacaoRepository;

    public AppointmentController(ConsultaRepository consultaRepository, NotificacaoRepository notificacaoRepository) {
        this.consultaRepository = consultaRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteAppointmentRequest request) {
        
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada com ID: " + id));

        consulta.setStatus("CONCLUIDA");
        consultaRepository.save(consulta);

        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notif = new Notificacao();
            notif.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            String notas = (request != null && request.getClinicalNotes() != null && !request.getClinicalNotes().isBlank())
                    ? " Observações: " + request.getClinicalNotes() : "";
            notif.setMensagem("A consulta do pet " + consulta.getPet().getNome() + " foi finalizada com sucesso." + notas);
            notificacaoRepository.save(notif);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable UUID id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta não encontrada com ID: " + id));

        consulta.setStatus("CANCELADA");
        consultaRepository.save(consulta);

        if (consulta.getPet() != null && consulta.getPet().getTutor() != null && consulta.getPet().getTutor().getContaAcesso() != null) {
            Notificacao notif = new Notificacao();
            notif.setContaAcesso(consulta.getPet().getTutor().getContaAcesso());
            notif.setMensagem("A consulta do pet " + consulta.getPet().getNome() + " foi cancelada com sucesso.");
            notificacaoRepository.save(notif);
        }

        return ResponseEntity.ok().build();
    }
}

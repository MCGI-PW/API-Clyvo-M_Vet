package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.CompleteAppointmentRequest;
import com.clyvo.veterinary.dto.ResumoConsultaResponse;
import com.clyvo.veterinary.dto.TranscricaoRequest;
import com.clyvo.veterinary.services.ConsultaService;
import com.clyvo.veterinary.services.TranscricaoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final ConsultaService consultaService;
    private final TranscricaoService transcricaoService;

    public AppointmentController(ConsultaService consultaService, TranscricaoService transcricaoService) {
        this.consultaService = consultaService;
        this.transcricaoService = transcricaoService;
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeAppointment(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteAppointmentRequest request) {
        String notes = (request != null) ? request.getClinicalNotes() : null;
        consultaService.completeConsulta(id, getLoggedAccountId(), notes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(@PathVariable UUID id) {
        consultaService.cancelarConsulta(id, getLoggedAccountId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Gera, com IA, o resumo clínico estruturado a partir da transcrição da teleconsulta")
    @PostMapping("/{id}/ai-summary")
    public ResponseEntity<ResumoConsultaResponse> generateAiSummary(@PathVariable UUID id,
                                                                    @Valid @RequestBody TranscricaoRequest request) {
        return ResponseEntity.ok(ResumoConsultaResponse.of(
                transcricaoService.gerarResumo(id, getLoggedAccountId(), request.getTranscript())));
    }

    @Operation(summary = "Lista as transcrições e resumos de IA já gerados para a consulta")
    @GetMapping("/{id}/ai-summary")
    public ResponseEntity<List<ResumoConsultaResponse>> listAiSummaries(@PathVariable UUID id) {
        return ResponseEntity.ok(transcricaoService.listarPorConsulta(id, getLoggedAccountId()).stream()
                .map(ResumoConsultaResponse::of)
                .toList());
    }
}

package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.dto.ConsultaRequest;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.services.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

/** Erros de domínio (400/403/404) são convertidos em JSON pelo GlobalExceptionHandler. */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listConsultas() {
        return ResponseEntity.ok(consultaService.listConsultasByConta(getLoggedAccountId()));
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@Valid @RequestBody ConsultaRequest request) {
        consultaService.createConsulta(request.toEntity(), getLoggedAccountId());
        return ResponseEntity.ok().build();
    }

    // PUT, POST e DELETE mantidos por retrocompatibilidade com versões do aplicativo mobile
    @RequestMapping(value = "/{id}/cancelar", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> cancelarConsulta(@PathVariable UUID id) {
        consultaService.cancelarConsulta(id, getLoggedAccountId());
        return ResponseEntity.ok(Map.of("message", "Consulta cancelada com sucesso!"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelarConsultaDelete(@PathVariable UUID id) {
        return cancelarConsulta(id);
    }
}

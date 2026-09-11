package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.services.ConsultaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    private UUID getLoggedAccountId() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(idContaStr);
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listConsultas() {
        return ResponseEntity.ok(consultaService.listConsultasByConta(getLoggedAccountId()));
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody Consulta consulta) {
        consultaService.createConsulta(consulta, getLoggedAccountId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarConsulta(@PathVariable UUID id) {
        return handleCancelamento(id);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarConsultaPost(@PathVariable UUID id) {
        return handleCancelamento(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarConsultaDelete(@PathVariable UUID id) {
        return handleCancelamento(id);
    }

    private ResponseEntity<?> handleCancelamento(UUID id) {
        try {
            consultaService.cancelarConsulta(id, getLoggedAccountId());
            return ResponseEntity.ok(Map.of("message", "Consulta cancelada com sucesso!"));
        } catch (com.clyvo.veterinary.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (com.clyvo.veterinary.exceptions.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (com.clyvo.veterinary.exceptions.BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage() != null && e.getMessage().contains("Acesso negado")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage() != null && e.getMessage().contains("já concluída")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            throw e;
        }
    }
}

package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.AutorizacaoAcessoPet;
import com.clyvo.veterinary.services.AutorizacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/autorizacoes")
public class AutorizacaoController {

    private final AutorizacaoService autorizacaoService;

    public AutorizacaoController(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    private UUID getLoggedAccountId() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(idContaStr);
    }

    @GetMapping
    public ResponseEntity<List<AutorizacaoAcessoPet>> listAutorizacoes() {
        return ResponseEntity.ok(autorizacaoService.listAutorizacoesByConta(getLoggedAccountId()));
    }

    @PutMapping("/{id}/revogar")
    public ResponseEntity<?> revogarAutorizacao(@PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String motivo = (body != null && body.containsKey("motivo")) ? body.get("motivo") : null;
        try {
            autorizacaoService.revogarAutorizacao(id, getLoggedAccountId(), motivo);
            return ResponseEntity.ok(Map.of("message", "Autorização revogada com sucesso e consultas ativas canceladas."));
        } catch (com.clyvo.veterinary.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (com.clyvo.veterinary.exceptions.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("não encontrada")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage() != null && e.getMessage().contains("Acesso negado")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            throw e;
        }
    }
}

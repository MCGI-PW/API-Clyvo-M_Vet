package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.AutorizacaoAcessoPet;
import com.clyvo.veterinary.services.AutorizacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@RestController
@RequestMapping("/api/autorizacoes")
public class AutorizacaoController {

    private final AutorizacaoService autorizacaoService;

    public AutorizacaoController(AutorizacaoService autorizacaoService) {
        this.autorizacaoService = autorizacaoService;
    }

    @GetMapping
    public ResponseEntity<List<AutorizacaoAcessoPet>> listAutorizacoes() {
        return ResponseEntity.ok(autorizacaoService.listAutorizacoesByConta(getLoggedAccountId()));
    }

    @PutMapping("/{id}/revogar")
    public ResponseEntity<Map<String, String>> revogarAutorizacao(@PathVariable UUID id,
                                                                  @RequestBody(required = false) Map<String, String> body) {
        String motivo = (body != null) ? body.get("motivo") : null;
        autorizacaoService.revogarAutorizacao(id, getLoggedAccountId(), motivo);
        return ResponseEntity.ok(Map.of("message", "Autorização revogada com sucesso e consultas ativas canceladas."));
    }
}

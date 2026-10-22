package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Notificacao;
import com.clyvo.veterinary.services.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listNotificacoes() {
        return ResponseEntity.ok(notificacaoService.listByConta(getLoggedAccountId()));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas() {
        return ResponseEntity.ok(Map.of("naoLidas", notificacaoService.contarNaoLidas(getLoggedAccountId())));
    }

    @PutMapping("/lidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas() {
        return ResponseEntity.ok(Map.of("atualizadas", notificacaoService.marcarTodasComoLidas(getLoggedAccountId())));
    }
}

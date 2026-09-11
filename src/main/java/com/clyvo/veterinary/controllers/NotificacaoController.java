package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Notificacao;
import com.clyvo.veterinary.services.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {
    
    private final NotificacaoService notificacaoService;
    
    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    private UUID getLoggedAccountId() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(idContaStr);
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listNotificacoes() {
        return ResponseEntity.ok(notificacaoService.listByConta(getLoggedAccountId()));
    }
}

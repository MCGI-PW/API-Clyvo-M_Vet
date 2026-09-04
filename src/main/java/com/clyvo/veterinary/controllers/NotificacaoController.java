package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Notificacao;
import com.clyvo.veterinary.repositories.NotificacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {
    
    private final NotificacaoRepository notificacaoRepository;
    
    public NotificacaoController(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notificacao>> listNotificacoes() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        return ResponseEntity.ok(notificacaoRepository.findByContaAcessoIdContaOrderByDataCriacaoDesc(idConta));
    }
}

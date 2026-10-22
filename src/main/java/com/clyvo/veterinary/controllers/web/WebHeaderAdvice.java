package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.SecurityUtils;
import com.clyvo.veterinary.services.NotificacaoService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Disponibiliza para o cabeçalho de todas as telas Web o total de notificações não lidas,
 * sem que cada controller precise repetir a consulta.
 */
@ControllerAdvice(basePackageClasses = WebHeaderAdvice.class)
public class WebHeaderAdvice {

    private final NotificacaoService notificacaoService;

    public WebHeaderAdvice(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @ModelAttribute("notificacoesNaoLidas")
    public long notificacoesNaoLidas() {
        return SecurityUtils.findLoggedAccountId()
                .map(notificacaoService::contarNaoLidas)
                .orElse(0L);
    }
}

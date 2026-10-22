package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.WebSession;
import com.clyvo.veterinary.services.NotificacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

/** Caixa de entrada de notificações, comum aos três perfis (tutor, veterinário e clínica). */
@Controller
@RequestMapping("/web/notificacoes")
public class WebNotificacaoController {

    private static final String PAGE = "/web/notificacoes";

    private final NotificacaoService notificacaoService;

    public WebNotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        model.addAttribute("notificacoes", notificacaoService.listByConta(getLoggedAccountId()));
        model.addAttribute("dashboardPath", WebSession.dashboardPath((String) session.getAttribute(WebSession.ROLE)));
        return "web/notificacoes";
    }

    @PostMapping("/lidas")
    public String marcarTodasComoLidas(RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, PAGE, "msg.notificacoes.read",
                () -> notificacaoService.marcarTodasComoLidas(getLoggedAccountId()));
    }
}

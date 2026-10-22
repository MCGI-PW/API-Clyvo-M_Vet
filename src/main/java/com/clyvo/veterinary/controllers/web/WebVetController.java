package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.services.ConsultaService;
import com.clyvo.veterinary.services.VeterinarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.StringJoiner;
import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@Controller
@RequestMapping("/web/vet")
@PreAuthorize("hasRole('VETERINARIO')")
public class WebVetController {

    private static final String DASHBOARD = "/web/vet/dashboard";

    private final ConsultaService consultaService;
    private final VeterinarioService veterinarioService;

    public WebVetController(ConsultaService consultaService, VeterinarioService veterinarioService) {
        this.consultaService = consultaService;
        this.veterinarioService = veterinarioService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UUID idConta = getLoggedAccountId();

        veterinarioService.findByConta(idConta).ifPresent(vet -> model.addAttribute("veterinario", vet));
        model.addAttribute("consultas", consultaService.listConsultasByConta(idConta));
        model.addAttribute("prontuarios", consultaService.listProntuariosByConta(idConta));
        return "web/dashboard-vet";
    }

    @PostMapping("/consultas/{id}/concluir")
    public String concluirConsulta(@PathVariable UUID id,
                                   @RequestParam(required = false) String diagnostico,
                                   @RequestParam(required = false) String prescricao,
                                   @RequestParam(required = false) String clinicalNotes,
                                   RedirectAttributes redirect) {
        String notas = montarProntuario(diagnostico, prescricao, clinicalNotes);
        return WebFeedback.execute(redirect, DASHBOARD, "msg.consulta.completed",
                () -> consultaService.completeConsulta(id, getLoggedAccountId(), notas));
    }

    @PostMapping("/consultas/{id}/cancelar")
    public String cancelarConsulta(@PathVariable UUID id, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.consulta.cancelled",
                () -> consultaService.cancelarConsulta(id, getLoggedAccountId()));
    }

    /** Estrutura o texto do prontuário a partir dos campos do formulário clínico. */
    private static String montarProntuario(String diagnostico, String prescricao, String observacoes) {
        StringJoiner notas = new StringJoiner("\n");
        adicionarSecao(notas, "Diagnóstico", diagnostico);
        adicionarSecao(notas, "Prescrição", prescricao);
        adicionarSecao(notas, "Observações", observacoes);
        return notas.toString();
    }

    private static void adicionarSecao(StringJoiner notas, String titulo, String valor) {
        if (valor != null && !valor.isBlank()) {
            notas.add(titulo + ": " + valor.trim());
        }
    }
}

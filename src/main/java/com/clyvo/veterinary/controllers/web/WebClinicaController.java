package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.models.Clinica;
import com.clyvo.veterinary.services.ClinicaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@Controller
@RequestMapping("/web/clinica")
@PreAuthorize("hasRole('CLINICA')")
public class WebClinicaController {

    private static final String DASHBOARD = "/web/clinica/dashboard";

    private final ClinicaService clinicaService;

    public WebClinicaController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    private Clinica getLoggedClinica() {
        return clinicaService.getClinicaByConta(getLoggedAccountId());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Clinica clinica = getLoggedClinica();
        UUID idClinica = clinica.getIdClinica();

        model.addAttribute("clinica", clinica);
        model.addAttribute("vinculos", clinicaService.listMeusVeterinarios(idClinica));
        // A transferência só aceita médicos com vínculo ativo, então o select lista apenas a equipe atual
        model.addAttribute("equipeAtiva", clinicaService.listVeterinariosDaClinica(idClinica));
        model.addAttribute("todosVeterinarios", clinicaService.listVeterinariosDisponiveis(idClinica));
        model.addAttribute("consultas", clinicaService.listConsultasDaClinica(idClinica));
        model.addAttribute("autorizacoes", clinicaService.listAutorizacoesDaClinica(idClinica));
        return "web/dashboard-clinica";
    }

    @PostMapping("/veterinarios/vincular")
    public String vincularVeterinario(@RequestParam UUID idVeterinario, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.vet.linked",
                () -> clinicaService.vincularVeterinario(getLoggedClinica(), idVeterinario));
    }

    @PostMapping("/veterinarios/{idVinculo}/desvincular")
    public String desvincularVeterinario(@PathVariable UUID idVinculo, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.vet.unlinked",
                () -> clinicaService.desvincularVeterinario(getLoggedClinica().getIdClinica(), idVinculo));
    }

    @PostMapping("/autorizacoes/transferir")
    public String transferirAutorizacao(@RequestParam UUID idAutorizacao,
                                        @RequestParam UUID idNovoVeterinario,
                                        RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.autorizacao.transferred",
                () -> clinicaService.transferirAutorizacao(getLoggedClinica(), idAutorizacao, idNovoVeterinario));
    }
}

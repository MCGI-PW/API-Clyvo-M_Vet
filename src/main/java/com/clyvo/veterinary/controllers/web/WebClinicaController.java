package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.services.ClinicaService;
import com.clyvo.veterinary.services.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/web/clinica")
public class WebClinicaController {

    private final ClinicaService clinicaService;
    private final VeterinarioService veterinarioService;
    private final JwtUtil jwtUtil;

    public WebClinicaController(ClinicaService clinicaService,
                                VeterinarioService veterinarioService,
                                JwtUtil jwtUtil) {
        this.clinicaService = clinicaService;
        this.veterinarioService = veterinarioService;
        this.jwtUtil = jwtUtil;
    }

    private UUID getAccountIdFromSession(HttpSession session) {
        String token = (String) session.getAttribute("jwtToken");
        if (token == null || !jwtUtil.validateToken(token)) {
            return null;
        }
        return UUID.fromString(jwtUtil.extractIdConta(token));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String linked,
                            @RequestParam(required = false) String unlinked,
                            @RequestParam(required = false) String transferred,
                            @RequestParam(required = false) String error) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) {
            return "redirect:/web/login";
        }

        Optional<Clinica> clinicaOpt = clinicaService.findClinicaByConta(idConta);
        if (clinicaOpt.isEmpty()) {
            return "redirect:/web/login?error=Acesso restrito a contas do tipo Clínica.";
        }

        Clinica clinica = clinicaOpt.get();
        model.addAttribute("clinica", clinica);
        model.addAttribute("vinculos", clinicaService.listMeusVeterinarios(clinica.getIdClinica()));
        model.addAttribute("todosVeterinarios", veterinarioService.listAll());
        model.addAttribute("consultas", clinicaService.listConsultasDaClinica(clinica.getIdClinica()));
        model.addAttribute("autorizacoes", clinicaService.listAutorizacoesDaClinica(clinica.getIdClinica()));
        model.addAttribute("pacientes", clinicaService.listPacientesDaClinica(clinica.getIdClinica()));

        if (linked != null) model.addAttribute("successMsg", "Veterinário vinculado com sucesso à unidade!");
        if (unlinked != null) model.addAttribute("successMsg", "Vínculo desativado com sucesso.");
        if (transferred != null) model.addAttribute("successMsg", "Atendimento e autorização transferidos com sucesso!");
        if (error != null) model.addAttribute("errorMsg", error);

        return "web/dashboard-clinica";
    }

    @PostMapping("/veterinarios/vincular")
    public String vincularVeterinario(@RequestParam UUID idVeterinario, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        Optional<Clinica> clinicaOpt = clinicaService.findClinicaByConta(idConta);
        if (clinicaOpt.isEmpty()) return "redirect:/web/login";

        try {
            clinicaService.vincularVeterinario(clinicaOpt.get().getIdClinica(), idVeterinario);
            return "redirect:/web/clinica/dashboard?linked=true";
        } catch (Exception e) {
            return "redirect:/web/clinica/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/veterinarios/{idVinculo}/desvincular")
    public String desvincularVeterinario(@PathVariable UUID idVinculo, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        Optional<Clinica> clinicaOpt = clinicaService.findClinicaByConta(idConta);
        if (clinicaOpt.isEmpty()) return "redirect:/web/login";

        try {
            clinicaService.desvincularVeterinario(clinicaOpt.get().getIdClinica(), idVinculo);
            return "redirect:/web/clinica/dashboard?unlinked=true";
        } catch (Exception e) {
            return "redirect:/web/clinica/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/autorizacoes/{idAutorizacao}/transferir")
    public String transferirAutorizacao(@PathVariable UUID idAutorizacao,
                                        @RequestParam UUID idNovoVeterinario,
                                        HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        Optional<Clinica> clinicaOpt = clinicaService.findClinicaByConta(idConta);
        if (clinicaOpt.isEmpty()) return "redirect:/web/login";

        try {
            clinicaService.transferirAutorizacao(clinicaOpt.get().getIdClinica(), idAutorizacao, idNovoVeterinario);
            return "redirect:/web/clinica/dashboard?transferred=true";
        } catch (Exception e) {
            return "redirect:/web/clinica/dashboard?error=" + e.getMessage();
        }
    }
}

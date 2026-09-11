package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Veterinario;
import com.clyvo.veterinary.services.ConsultaService;
import com.clyvo.veterinary.services.VeterinarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/web/vet")
public class WebVetController {

    private final ConsultaService consultaService;
    private final VeterinarioService veterinarioService;
    private final JwtUtil jwtUtil;

    public WebVetController(ConsultaService consultaService,
                            VeterinarioService veterinarioService,
                            JwtUtil jwtUtil) {
        this.consultaService = consultaService;
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
                            @RequestParam(required = false) String completed,
                            @RequestParam(required = false) String cancelled,
                            @RequestParam(required = false) String error) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) {
            return "redirect:/web/login";
        }

        Optional<Veterinario> vetOpt = veterinarioService.findByConta(idConta);
        vetOpt.ifPresent(vet -> model.addAttribute("veterinario", vet));

        List<Consulta> consultas = consultaService.listConsultasByConta(idConta);
        model.addAttribute("consultas", consultas);

        if (completed != null) model.addAttribute("successMsg", "Atendimento clínico concluído com sucesso!");
        if (cancelled != null) model.addAttribute("successMsg", "Consulta cancelada com sucesso.");
        if (error != null) model.addAttribute("errorMsg", error);

        return "web/dashboard-vet";
    }

    @PostMapping("/consultas/{id}/concluir")
    public String concluirConsulta(@PathVariable UUID id,
                                   @RequestParam(required = false) String clinicalNotes,
                                   HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            consultaService.completeConsulta(id, idConta, clinicalNotes);
            return "redirect:/web/vet/dashboard?completed=true";
        } catch (Exception e) {
            return "redirect:/web/vet/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/consultas/{id}/cancelar")
    public String cancelarConsulta(@PathVariable UUID id, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            consultaService.cancelarConsulta(id, idConta);
            return "redirect:/web/vet/dashboard?cancelled=true";
        } catch (Exception e) {
            return "redirect:/web/vet/dashboard?error=" + e.getMessage();
        }
    }
}

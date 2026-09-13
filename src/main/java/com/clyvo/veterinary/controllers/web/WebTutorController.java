package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.config.JwtUtil;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/web/tutor")
@PreAuthorize("hasRole('TUTOR')")
public class WebTutorController {

    private final PetService petService;
    private final ConsultaService consultaService;
    private final AutorizacaoService autorizacaoService;
    private final ClinicaService clinicaService;
    private final VeterinarioService veterinarioService;
    private final RacaService racaService;
    private final TutorService tutorService;
    private final JwtUtil jwtUtil;

    public WebTutorController(PetService petService,
                              ConsultaService consultaService,
                              AutorizacaoService autorizacaoService,
                              ClinicaService clinicaService,
                              VeterinarioService veterinarioService,
                              RacaService racaService,
                              TutorService tutorService,
                              JwtUtil jwtUtil) {
        this.petService = petService;
        this.consultaService = consultaService;
        this.autorizacaoService = autorizacaoService;
        this.clinicaService = clinicaService;
        this.veterinarioService = veterinarioService;
        this.racaService = racaService;
        this.tutorService = tutorService;
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
                            @RequestParam(required = false) String petCreated,
                            @RequestParam(required = false) String consultaCreated,
                            @RequestParam(required = false) String cancelled,
                            @RequestParam(required = false) String revoked,
                            @RequestParam(required = false) String error) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) {
            return "redirect:/web/login";
        }

        Optional<Tutor> tutorOpt = tutorService.findByConta(idConta);
        tutorOpt.ifPresent(tutor -> model.addAttribute("tutor", tutor));

        model.addAttribute("pets", petService.listPetsByConta(idConta));
        model.addAttribute("consultas", consultaService.listConsultasByConta(idConta));
        model.addAttribute("autorizacoes", autorizacaoService.listAutorizacoesByConta(idConta));
        model.addAttribute("clinicas", clinicaService.listClinicasAtivas());
        model.addAttribute("veterinarios", veterinarioService.listAll());
        model.addAttribute("racas", racaService.listAll());

        model.addAttribute("novoPet", new Pet());
        model.addAttribute("novaConsulta", new Consulta());

        if (petCreated != null) model.addAttribute("successMsg", "Pet cadastrado com sucesso!");
        if (consultaCreated != null) model.addAttribute("successMsg", "Consulta agendada com sucesso!");
        if (cancelled != null) model.addAttribute("successMsg", "Consulta cancelada com sucesso.");
        if (revoked != null) model.addAttribute("successMsg", "Autorização revogada com sucesso.");
        if (error != null) model.addAttribute("errorMsg", error);

        return "web/dashboard-tutor";
    }

    @PostMapping("/pets")
    public String cadastrarPet(@ModelAttribute("novoPet") Pet pet, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            petService.createPet(pet, idConta);
            return "redirect:/web/tutor/dashboard?petCreated=true";
        } catch (Exception e) {
            return "redirect:/web/tutor/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/consultas")
    public String agendarConsulta(@ModelAttribute("novaConsulta") Consulta consulta, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            consultaService.createConsulta(consulta, idConta);
            return "redirect:/web/tutor/dashboard?consultaCreated=true";
        } catch (Exception e) {
            return "redirect:/web/tutor/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/consultas/{id}/cancelar")
    public String cancelarConsulta(@PathVariable UUID id, HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            consultaService.cancelarConsulta(id, idConta);
            return "redirect:/web/tutor/dashboard?cancelled=true";
        } catch (Exception e) {
            return "redirect:/web/tutor/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/autorizacoes/{id}/revogar")
    public String revogarAutorizacao(@PathVariable UUID id,
                                     @RequestParam(required = false) String motivo,
                                     HttpSession session) {
        UUID idConta = getAccountIdFromSession(session);
        if (idConta == null) return "redirect:/web/login";

        try {
            autorizacaoService.revogarAutorizacao(id, idConta, motivo);
            return "redirect:/web/tutor/dashboard?revoked=true";
        } catch (Exception e) {
            return "redirect:/web/tutor/dashboard?error=" + e.getMessage();
        }
    }
}

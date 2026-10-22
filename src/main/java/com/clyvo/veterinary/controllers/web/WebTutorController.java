package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.services.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

@Controller
@RequestMapping("/web/tutor")
@PreAuthorize("hasRole('TUTOR')")
public class WebTutorController {

    private static final String DASHBOARD = "/web/tutor/dashboard";

    private final PetService petService;
    private final ConsultaService consultaService;
    private final AutorizacaoService autorizacaoService;
    private final ClinicaService clinicaService;
    private final VeterinarioService veterinarioService;
    private final RacaService racaService;
    private final TutorService tutorService;

    public WebTutorController(PetService petService,
                              ConsultaService consultaService,
                              AutorizacaoService autorizacaoService,
                              ClinicaService clinicaService,
                              VeterinarioService veterinarioService,
                              RacaService racaService,
                              TutorService tutorService) {
        this.petService = petService;
        this.consultaService = consultaService;
        this.autorizacaoService = autorizacaoService;
        this.clinicaService = clinicaService;
        this.veterinarioService = veterinarioService;
        this.racaService = racaService;
        this.tutorService = tutorService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        UUID idConta = getLoggedAccountId();

        tutorService.findByConta(idConta).ifPresent(tutor -> model.addAttribute("tutor", tutor));
        model.addAttribute("pets", petService.listPetsByConta(idConta));
        model.addAttribute("consultas", consultaService.listConsultasByConta(idConta));
        model.addAttribute("autorizacoes", autorizacaoService.listAutorizacoesByConta(idConta));
        model.addAttribute("prontuarios", consultaService.listProntuariosByConta(idConta));
        model.addAttribute("clinicas", clinicaService.listClinicasAtivas());
        model.addAttribute("veterinarios", veterinarioService.listAll());
        model.addAttribute("racas", racaService.listAll());
        model.addAttribute("novoPet", new Pet());
        model.addAttribute("novaConsulta", new Consulta());
        return "web/dashboard-tutor";
    }

    @PostMapping("/pets")
    public String cadastrarPet(@ModelAttribute("novoPet") Pet pet, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.pet.created",
                () -> petService.createPet(pet, getLoggedAccountId()));
    }

    @PostMapping("/consultas")
    public String agendarConsulta(@ModelAttribute("novaConsulta") Consulta consulta, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.consulta.created",
                () -> consultaService.createConsulta(consulta, getLoggedAccountId()));
    }

    @PostMapping("/consultas/{id}/cancelar")
    public String cancelarConsulta(@PathVariable UUID id, RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.consulta.cancelled",
                () -> consultaService.cancelarConsulta(id, getLoggedAccountId()));
    }

    @PostMapping("/autorizacoes/{id}/revogar")
    public String revogarAutorizacao(@PathVariable UUID id,
                                     @RequestParam(required = false) String motivo,
                                     RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.autorizacao.revoked",
                () -> autorizacaoService.revogarAutorizacao(id, getLoggedAccountId(), motivo));
    }

    @PostMapping("/autorizacoes")
    public String concederAutorizacao(@RequestParam UUID idPet,
                                      @RequestParam(required = false) UUID idVeterinario,
                                      @RequestParam(required = false) UUID idClinica,
                                      RedirectAttributes redirect) {
        return WebFeedback.execute(redirect, DASHBOARD, "msg.autorizacao.granted",
                () -> autorizacaoService.concederAutorizacao(idPet, idVeterinario, idClinica, getLoggedAccountId()));
    }
}

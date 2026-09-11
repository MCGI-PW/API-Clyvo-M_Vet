package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.services.ClinicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    private final ClinicaService clinicaService;

    public ClinicaController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    private Optional<Clinica> getLoggedClinica() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        return clinicaService.findClinicaByConta(idConta);
    }

    @GetMapping
    public ResponseEntity<List<Clinica>> listClinicasAtivas() {
        return ResponseEntity.ok(clinicaService.listClinicasAtivas());
    }

    @GetMapping("/minha")
    public ResponseEntity<?> getMinhaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Apenas contas do tipo CLÍNICA podem acessar este recurso."));
        }
        return ResponseEntity.ok(clinicaOpt.get());
    }

    @GetMapping("/{idClinica}/veterinarios")
    public ResponseEntity<List<Veterinario>> listVeterinariosDaClinica(@PathVariable UUID idClinica) {
        return ResponseEntity.ok(clinicaService.listVeterinariosDaClinica(idClinica));
    }

    @GetMapping("/meus-veterinarios")
    public ResponseEntity<?> listMeusVeterinarios() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        return ResponseEntity.ok(clinicaService.listMeusVeterinarios(clinicaOpt.get().getIdClinica()));
    }

    @PostMapping("/veterinarios/vincular")
    public ResponseEntity<?> vincularVeterinario(@RequestBody Map<String, String> request) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        String idVetStr = request.get("idVeterinario");
        if (idVetStr == null || idVetStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do veterinário é obrigatório."));
        }

        clinicaService.vincularVeterinario(clinicaOpt.get(), UUID.fromString(idVetStr));
        return ResponseEntity.ok(Map.of("message", "Veterinário vinculado com sucesso à clínica."));
    }

    @PutMapping("/veterinarios/{idVinculo}/desvincular")
    public ResponseEntity<?> desvincularVeterinario(@PathVariable UUID idVinculo) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        clinicaService.desvincularVeterinario(clinicaOpt.get().getIdClinica(), idVinculo);
        return ResponseEntity.ok(Map.of("message", "Vínculo desativado com sucesso."));
    }

    @GetMapping("/consultas")
    public ResponseEntity<?> listConsultasDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        return ResponseEntity.ok(clinicaService.listConsultasDaClinica(clinicaOpt.get().getIdClinica()));
    }

    @GetMapping("/autorizacoes")
    public ResponseEntity<?> listAutorizacoesDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        return ResponseEntity.ok(clinicaService.listAutorizacoesDaClinica(clinicaOpt.get().getIdClinica()));
    }

    @PutMapping("/autorizacoes/{idAutorizacao}/transferir")
    public ResponseEntity<?> transferirAutorizacao(@PathVariable UUID idAutorizacao,
                                                   @RequestBody Map<String, String> body) {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }

        String idNovoVetStr = body.get("idNovoVeterinario");
        if (idNovoVetStr == null || idNovoVetStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID do novo veterinário é obrigatório."));
        }

        clinicaService.transferirAutorizacao(clinicaOpt.get(), idAutorizacao, UUID.fromString(idNovoVetStr));
        return ResponseEntity.ok(Map.of("message", "Autorização e consultas ativas transferidas com sucesso."));
    }

    @GetMapping("/pacientes")
    public ResponseEntity<?> listPacientesDaClinica() {
        Optional<Clinica> clinicaOpt = getLoggedClinica();
        if (clinicaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acesso restrito a clínicas."));
        }
        return ResponseEntity.ok(clinicaService.listPacientesDaClinica(clinicaOpt.get().getIdClinica()));
    }
}

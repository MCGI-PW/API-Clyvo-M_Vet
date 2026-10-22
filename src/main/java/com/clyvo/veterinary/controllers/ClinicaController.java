package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.models.*;
import com.clyvo.veterinary.services.ClinicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.clyvo.veterinary.config.SecurityUtils.getLoggedAccountId;

/** Endpoints da clínica autenticada; contas de outros perfis recebem 403 via ClinicaService.getClinicaByConta. */
@RestController
@RequestMapping("/api/clinicas")
public class ClinicaController {

    private final ClinicaService clinicaService;

    public ClinicaController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    private Clinica getLoggedClinica() {
        return clinicaService.getClinicaByConta(getLoggedAccountId());
    }

    @GetMapping
    public ResponseEntity<List<Clinica>> listClinicasAtivas() {
        return ResponseEntity.ok(clinicaService.listClinicasAtivas());
    }

    @GetMapping("/minha")
    public ResponseEntity<Clinica> getMinhaClinica() {
        return ResponseEntity.ok(getLoggedClinica());
    }

    @GetMapping("/{idClinica}/veterinarios")
    public ResponseEntity<List<Veterinario>> listVeterinariosDaClinica(@PathVariable UUID idClinica) {
        return ResponseEntity.ok(clinicaService.listVeterinariosDaClinica(idClinica));
    }

    @GetMapping("/meus-veterinarios")
    public ResponseEntity<List<VeterinarioClinica>> listMeusVeterinarios() {
        return ResponseEntity.ok(clinicaService.listMeusVeterinarios(getLoggedClinica().getIdClinica()));
    }

    @PostMapping("/veterinarios/vincular")
    public ResponseEntity<Map<String, String>> vincularVeterinario(@RequestBody Map<String, String> request) {
        Clinica clinica = getLoggedClinica();
        clinicaService.vincularVeterinario(clinica, requiredUuid(request, "idVeterinario", "ID do veterinário é obrigatório."));
        return ResponseEntity.ok(Map.of("message", "Veterinário vinculado com sucesso à clínica."));
    }

    @PutMapping("/veterinarios/{idVinculo}/desvincular")
    public ResponseEntity<Map<String, String>> desvincularVeterinario(@PathVariable UUID idVinculo) {
        clinicaService.desvincularVeterinario(getLoggedClinica().getIdClinica(), idVinculo);
        return ResponseEntity.ok(Map.of("message", "Vínculo desativado com sucesso."));
    }

    @GetMapping("/consultas")
    public ResponseEntity<List<Consulta>> listConsultasDaClinica() {
        return ResponseEntity.ok(clinicaService.listConsultasDaClinica(getLoggedClinica().getIdClinica()));
    }

    @GetMapping("/autorizacoes")
    public ResponseEntity<List<AutorizacaoAcessoPet>> listAutorizacoesDaClinica() {
        return ResponseEntity.ok(clinicaService.listAutorizacoesDaClinica(getLoggedClinica().getIdClinica()));
    }

    @PutMapping("/autorizacoes/{idAutorizacao}/transferir")
    public ResponseEntity<Map<String, String>> transferirAutorizacao(@PathVariable UUID idAutorizacao,
                                                                     @RequestBody Map<String, String> body) {
        Clinica clinica = getLoggedClinica();
        clinicaService.transferirAutorizacao(clinica, idAutorizacao,
                requiredUuid(body, "idNovoVeterinario", "ID do novo veterinário é obrigatório."));
        return ResponseEntity.ok(Map.of("message", "Autorização e consultas ativas transferidas com sucesso."));
    }

    @GetMapping("/pacientes")
    public ResponseEntity<List<Pet>> listPacientesDaClinica() {
        return ResponseEntity.ok(clinicaService.listPacientesDaClinica(getLoggedClinica().getIdClinica()));
    }

    private static UUID requiredUuid(Map<String, String> body, String field, String message) {
        String value = body != null ? body.get(field) : null;
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Identificador inválido: " + field);
        }
    }
}

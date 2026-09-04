package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {
    
    private final ConsultaRepository consultaRepository;
    private final TutorRepository tutorRepository;
    
    public ConsultaController(ConsultaRepository consultaRepository, TutorRepository tutorRepository) {
        this.consultaRepository = consultaRepository;
        this.tutorRepository = tutorRepository;
    }

    @GetMapping
    public ResponseEntity<List<Consulta>> listTutorConsultas() {
        String idContaStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID idConta = UUID.fromString(idContaStr);
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta).orElseThrow();
        
        return ResponseEntity.ok(consultaRepository.findByPetTutorIdTutor(tutor.getIdTutor()));
    }

    @PostMapping
    public ResponseEntity<Void> createConsulta(@RequestBody Consulta consulta) {
        consultaRepository.save(consulta);
        return ResponseEntity.ok().build();
    }
}

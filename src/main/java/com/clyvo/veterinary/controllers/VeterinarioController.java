package com.clyvo.veterinary.controllers;

import com.clyvo.veterinary.models.Veterinario;
import com.clyvo.veterinary.services.VeterinarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    private final VeterinarioService service;

    public VeterinarioController(VeterinarioService service) {
        this.service = service;
    }
    
    @GetMapping
    public List<Veterinario> listAll() {
        return service.listAll();
    }
}

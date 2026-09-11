package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Veterinario;
import com.clyvo.veterinary.repositories.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioService(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Veterinario> listAll() {
        return veterinarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Veterinario> findByConta(UUID idConta) {
        return veterinarioRepository.findByContaAcessoIdConta(idConta);
    }

    @Transactional(readOnly = true)
    public Optional<Veterinario> findById(UUID idVet) {
        return veterinarioRepository.findById(idVet);
    }
}

package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Raca;
import com.clyvo.veterinary.repositories.RacaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RacaService {

    private final RacaRepository racaRepository;

    public RacaService(RacaRepository racaRepository) {
        this.racaRepository = racaRepository;
    }

    @Transactional(readOnly = true)
    public List<Raca> listAll() {
        return racaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Raca> listByEspecie(UUID idEspecie) {
        return racaRepository.findByEspecieIdEspecie(idEspecie);
    }
}

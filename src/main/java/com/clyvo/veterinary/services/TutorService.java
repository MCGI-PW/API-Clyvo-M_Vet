package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Transactional(readOnly = true)
    public List<Tutor> listAll() {
        return tutorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Tutor> findByConta(UUID idConta) {
        return tutorRepository.findByContaAcessoIdConta(idConta);
    }

    @Transactional(readOnly = true)
    public Optional<Tutor> findById(UUID idTutor) {
        return tutorRepository.findById(idTutor);
    }
}

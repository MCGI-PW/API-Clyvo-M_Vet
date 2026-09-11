package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.PetRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetService(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @Transactional
    public Pet createPet(Pet pet, UUID idConta) {
        Tutor tutor = tutorRepository.findByContaAcessoIdConta(idConta)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado para a conta logada."));

        pet.setIdPet(null);
        pet.setTutor(tutor);
        pet.setAtivo(true);
        return petRepository.save(pet);
    }

    @Transactional(readOnly = true)
    public List<Pet> listPetsByConta(UUID idConta) {
        Optional<Tutor> tutorOpt = tutorRepository.findByContaAcessoIdConta(idConta);
        if (tutorOpt.isEmpty()) {
            return Collections.emptyList();
        }
        return petRepository.findByTutorIdTutor(tutorOpt.get().getIdTutor());
    }

    @Transactional(readOnly = true)
    public Optional<Pet> findById(UUID idPet) {
        return petRepository.findById(idPet);
    }

    @Transactional(readOnly = true)
    public List<Pet> listAll() {
        return petRepository.findAll();
    }
}

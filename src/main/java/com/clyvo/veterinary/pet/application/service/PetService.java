package com.clyvo.veterinary.pet.application.service;

import com.clyvo.veterinary.pet.application.dto.CreatePetRequest;
import com.clyvo.veterinary.pet.application.dto.PetResponse;
import com.clyvo.veterinary.pet.application.dto.UpdatePetRequest;
import com.clyvo.veterinary.pet.application.mapper.PetDtoMapper;
import com.clyvo.veterinary.pet.application.port.in.PetUseCase;
import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.domain.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PetService implements PetUseCase {

    private final PetRepository petRepository;
    private final PetDtoMapper petDtoMapper;

    public PetService(PetRepository petRepository, PetDtoMapper petDtoMapper) {
        this.petRepository = petRepository;
        this.petDtoMapper = petDtoMapper;
    }

    @Override
    public PetResponse createPet(CreatePetRequest request) {
        Pet pet = Pet.create(request.tutorId(), request.name(), request.species(), request.breed(), request.birthDate());
        if (request.weight() != null || request.color() != null || request.profilePictureUrl() != null) {
            pet.updateInfo(request.name(), request.breed(), request.birthDate(), request.weight(), request.color(), request.profilePictureUrl());
        }
        Pet saved = petRepository.save(pet);
        return petDtoMapper.toResponse(saved, null, null);
    }

    @Override
    public PetResponse updatePet(UUID id, UpdatePetRequest request) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        pet.updateInfo(request.name(), request.breed(), request.birthDate(), request.weight(), request.color(), request.profilePictureUrl());
        Pet saved = petRepository.save(pet);
        return petDtoMapper.toResponse(saved, null, null);
    }

    @Override
    public PetResponse getPet(UUID id) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        return petDtoMapper.toResponse(pet, null, null);
    }

    @Override
    public List<PetResponse> listPetsByTutor(UUID tutorId) {
        return petRepository.findByTutorId(tutorId).stream()
                .map(p -> petDtoMapper.toResponse(p, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<PetResponse> listAll() {
        return petRepository.findAll().stream()
                .map(p -> petDtoMapper.toResponse(p, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public void deletePet(UUID id) {
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        pet.deactivate();
        petRepository.save(pet);
    }
}

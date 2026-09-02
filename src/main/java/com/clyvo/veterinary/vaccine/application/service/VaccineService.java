package com.clyvo.veterinary.vaccine.application.service;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.domain.repository.PetRepository;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.application.mapper.VaccineDtoMapper;
import com.clyvo.veterinary.vaccine.application.port.in.VaccineUseCase;
import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import com.clyvo.veterinary.vaccine.domain.repository.VaccineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VaccineService implements VaccineUseCase {

    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;
    private final VaccineDtoMapper vaccineDtoMapper;

    public VaccineService(VaccineRepository vaccineRepository,
                          PetRepository petRepository,
                          VaccineDtoMapper vaccineDtoMapper) {
        this.vaccineRepository = vaccineRepository;
        this.petRepository = petRepository;
        this.vaccineDtoMapper = vaccineDtoMapper;
    }

    @Override
    @Transactional
    public VaccineResponse registerVaccine(CreateVaccineRequest request) {
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()));

        Vaccine vaccine = Vaccine.register(
                request.petId(),
                request.vaccineName(),
                request.manufacturer(),
                request.batchNumber(),
                request.appliedAt(),
                request.nextDoseAt(),
                request.notes()
        );

        Vaccine savedVaccine = vaccineRepository.save(vaccine);
        return vaccineDtoMapper.toResponse(savedVaccine, pet);
    }

    @Override
    public VaccineResponse getVaccine(UUID id) {
        Vaccine vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", id));
                
        Pet pet = petRepository.findById(vaccine.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", vaccine.getPetId()));

        return vaccineDtoMapper.toResponse(vaccine, pet);
    }

    @Override
    public List<VaccineResponse> listByPet(UUID petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        return vaccineRepository.findByPetId(petId).stream()
                .map(vaccine -> vaccineDtoMapper.toResponse(vaccine, pet))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteVaccine(UUID id) {
        if (!vaccineRepository.findById(id).isPresent()) {
            throw new ResourceNotFoundException("Vaccine", id);
        }
        vaccineRepository.deleteById(id);
    }
}

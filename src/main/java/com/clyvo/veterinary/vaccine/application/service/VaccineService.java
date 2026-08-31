package com.clyvo.veterinary.vaccine.application.service;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.domain.repository.PetRepository;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.application.mapper.VaccineDtoMapper;
import com.clyvo.veterinary.vaccine.application.port.in.VaccineUseCase;
import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import com.clyvo.veterinary.vaccine.domain.repository.VaccineRepository;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import com.clyvo.veterinary.veterinarian.domain.repository.VeterinarianRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VaccineService implements VaccineUseCase {

    private static final Logger log = LoggerFactory.getLogger(VaccineService.class);

    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final UserRepository userRepository;
    private final VaccineDtoMapper vaccineDtoMapper;

    public VaccineService(VaccineRepository vaccineRepository,
                          PetRepository petRepository,
                          VeterinarianRepository veterinarianRepository,
                          UserRepository userRepository,
                          VaccineDtoMapper vaccineDtoMapper) {
        this.vaccineRepository = vaccineRepository;
        this.petRepository = petRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.userRepository = userRepository;
        this.vaccineDtoMapper = vaccineDtoMapper;
    }

    @Override
    public VaccineResponse registerVaccine(UUID veterinarianId, CreateVaccineRequest request) {
        log.info("Registrando vacina {} para pet {}", request.vaccineName(), request.petId());

        Veterinarian vet = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian", veterinarianId));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()));

        Vaccine vaccine = Vaccine.register(
                request.petId(),
                veterinarianId,
                request.vaccineName(),
                request.manufacturer(),
                request.batchNumber(),
                request.appliedAt(),
                request.nextDoseAt()
        );

        if (request.notes() != null && !request.notes().isBlank()) {
            vaccine.addNotes(request.notes());
        }

        Vaccine saved = vaccineRepository.save(vaccine);
        User vetUser = userRepository.findById(vet.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", vet.getUserId()));

        log.info("Vacina registrada com id {}", saved.getId());
        return vaccineDtoMapper.toResponse(saved, pet, vet, vetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public VaccineResponse getVaccine(UUID vaccineId) {
        Vaccine vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", vaccineId));
        return buildResponse(vaccine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccineResponse> listByPet(UUID petId) {
        petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        return vaccineRepository.findByPetId(petId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccineResponse> listByVeterinarian(UUID veterinarianId) {
        return vaccineRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    public void deleteVaccine(UUID vaccineId) {
        vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", vaccineId));
        vaccineRepository.deleteById(vaccineId);
        log.info("Vacina {} deletada", vaccineId);
    }

    private VaccineResponse buildResponse(Vaccine vaccine) {
        Pet pet = petRepository.findById(vaccine.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", vaccine.getPetId()));
        Veterinarian vet = veterinarianRepository.findById(vaccine.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian", vaccine.getVeterinarianId()));
        User vetUser = userRepository.findById(vet.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", vet.getUserId()));
        return vaccineDtoMapper.toResponse(vaccine, pet, vet, vetUser);
    }
}

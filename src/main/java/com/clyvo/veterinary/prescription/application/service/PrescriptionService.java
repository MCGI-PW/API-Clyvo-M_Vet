package com.clyvo.veterinary.prescription.application.service;

import com.clyvo.veterinary.medicalrecord.domain.repository.MedicalRecordRepository;
import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.domain.repository.PetRepository;
import com.clyvo.veterinary.prescription.application.dto.CreatePrescriptionRequest;
import com.clyvo.veterinary.prescription.application.dto.PrescriptionResponse;
import com.clyvo.veterinary.prescription.application.mapper.PrescriptionDtoMapper;
import com.clyvo.veterinary.prescription.application.port.in.PrescriptionUseCase;
import com.clyvo.veterinary.prescription.domain.model.Prescription;
import com.clyvo.veterinary.prescription.domain.repository.PrescriptionRepository;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
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
public class PrescriptionService implements PrescriptionUseCase {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final UserRepository userRepository;
    private final PrescriptionDtoMapper prescriptionDtoMapper;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               MedicalRecordRepository medicalRecordRepository,
                               PetRepository petRepository,
                               VeterinarianRepository veterinarianRepository,
                               UserRepository userRepository,
                               PrescriptionDtoMapper prescriptionDtoMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.petRepository = petRepository;
        this.veterinarianRepository = veterinarianRepository;
        this.userRepository = userRepository;
        this.prescriptionDtoMapper = prescriptionDtoMapper;
    }

    @Override
    public PrescriptionResponse createPrescription(UUID veterinarianId, CreatePrescriptionRequest request) {
        log.info("Criando prescrição para o pet {} pelo vet {}", request.petId(), veterinarianId);

        Veterinarian vet = veterinarianRepository.findById(veterinarianId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian", veterinarianId));

        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", request.petId()));

        medicalRecordRepository.findById(request.medicalRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", request.medicalRecordId()));

        Prescription prescription = Prescription.create(
                request.medicalRecordId(),
                request.petId(),
                veterinarianId,
                request.medications(),
                request.generalInstructions(),
                request.validUntil()
        );

        Prescription saved = prescriptionRepository.save(prescription);
        User vetUser = userRepository.findById(vet.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", vet.getUserId()));

        log.info("Prescrição criada com id {}", saved.getId());
        return prescriptionDtoMapper.toResponse(saved, pet, vet, vetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(UUID prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", prescriptionId));
        return buildResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listByPet(UUID petId) {
        petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        return prescriptionRepository.findByPetId(petId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listByVeterinarian(UUID veterinarianId) {
        return prescriptionRepository.findByVeterinarianId(veterinarianId).stream()
                .map(this::buildResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getByMedicalRecord(UUID medicalRecordId) {
        Prescription prescription = prescriptionRepository.findByMedicalRecordId(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription para o prontuário", medicalRecordId));
        return buildResponse(prescription);
    }

    @Override
    public void deletePrescription(UUID prescriptionId) {
        prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", prescriptionId));
        prescriptionRepository.deleteById(prescriptionId);
        log.info("Prescrição {} deletada", prescriptionId);
    }

    private PrescriptionResponse buildResponse(Prescription prescription) {
        Pet pet = petRepository.findById(prescription.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", prescription.getPetId()));
        Veterinarian vet = veterinarianRepository.findById(prescription.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian", prescription.getVeterinarianId()));
        User vetUser = userRepository.findById(vet.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", vet.getUserId()));
        return prescriptionDtoMapper.toResponse(prescription, pet, vet, vetUser);
    }
}

package com.clyvo.veterinary.prescription.application.mapper;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.prescription.application.dto.PrescriptionResponse;
import com.clyvo.veterinary.prescription.domain.model.Prescription;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionDtoMapper {

    public PrescriptionResponse toResponse(Prescription prescription, Pet pet, Veterinarian vet, User vetUser) {
        if (prescription == null) return null;
        
        String petName = pet != null ? pet.getName() : "Desconhecido";
        String vetName = vetUser != null ? vetUser.getName() : "Desconhecido";
        String vetCrm = vet != null ? vet.getCrm() : "Não informado";
        
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPetId(),
                petName,
                prescription.getVeterinarianId(),
                vetName,
                vetCrm,
                prescription.getMedications(),
                prescription.getGeneralInstructions(),
                prescription.getValidUntil(),
                prescription.isValid(),
                prescription.getCreatedAt()
        );
    }
}

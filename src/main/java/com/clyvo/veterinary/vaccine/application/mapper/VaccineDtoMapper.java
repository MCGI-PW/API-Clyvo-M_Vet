package com.clyvo.veterinary.vaccine.application.mapper;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import org.springframework.stereotype.Component;

@Component
public class VaccineDtoMapper {

    public VaccineResponse toResponse(Vaccine vaccine, Pet pet, Veterinarian vet, User vetUser) {
        return new VaccineResponse(
                vaccine.getId(),
                vaccine.getPetId(),
                pet.getName(),
                vaccine.getVeterinarianId(),
                vetUser.getName(),
                vaccine.getVaccineName(),
                vaccine.getManufacturer(),
                vaccine.getBatchNumber(),
                vaccine.getAppliedAt(),
                vaccine.getNextDoseAt(),
                vaccine.isNextDoseDue(),
                vaccine.getNotes(),
                vaccine.getCreatedAt()
        );
    }
}

package com.clyvo.veterinary.vaccine.application.mapper;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;
import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import org.springframework.stereotype.Component;

@Component
public class VaccineDtoMapper {

    public VaccineResponse toResponse(Vaccine vaccine, Pet pet) {
        return new VaccineResponse(
                vaccine.getId(),
                vaccine.getPetId(),
                pet.getName(),
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

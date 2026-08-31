package com.clyvo.veterinary.pet.application.mapper;

import com.clyvo.veterinary.pet.application.dto.PetResponse;
import com.clyvo.veterinary.pet.domain.model.Pet;
import org.springframework.stereotype.Component;

@Component
public class PetDtoMapper {

    public PetResponse toResponse(Pet pet, Object tutor, Object tutorUser) {
        String tutorName = "Tutor Name";
        return new PetResponse(
                pet.getId(),
                pet.getTutorId(),
                tutorName,
                pet.getName(),
                pet.getSpecies() != null ? pet.getSpecies().getDisplayName() : null,
                pet.getBreed(),
                pet.getBirthDate(),
                pet.getWeight(),
                pet.getColor(),
                pet.getProfilePictureUrl(),
                pet.isActive(),
                pet.getCreatedAt()
        );
    }
}

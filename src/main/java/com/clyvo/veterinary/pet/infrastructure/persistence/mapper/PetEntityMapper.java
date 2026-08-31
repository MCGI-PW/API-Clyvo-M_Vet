package com.clyvo.veterinary.pet.infrastructure.persistence.mapper;

import com.clyvo.veterinary.pet.domain.model.Pet;
import com.clyvo.veterinary.pet.infrastructure.persistence.entity.PetEntity;
import org.springframework.stereotype.Component;

@Component
public class PetEntityMapper {

    public PetEntity toEntity(Pet pet) {
        if (pet == null) return null;
        return PetEntity.builder()
                .id(pet.getId())
                .tutorId(pet.getTutorId())
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .weight(pet.getWeight())
                .color(pet.getColor())
                .profilePictureUrl(pet.getProfilePictureUrl())
                .active(pet.isActive())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }

    public Pet toDomain(PetEntity entity) {
        if (entity == null) return null;
        try {
            java.lang.reflect.Constructor<Pet> constructor = Pet.class.getDeclaredConstructor(
                    java.util.UUID.class, java.util.UUID.class, String.class, com.clyvo.veterinary.pet.domain.model.Species.class,
                    String.class, java.time.LocalDate.class, Double.class, String.class, String.class, boolean.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
                );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(), entity.getTutorId(), entity.getName(), entity.getSpecies(), entity.getBreed(),
                    entity.getBirthDate(), entity.getWeight(), entity.getColor(), entity.getProfilePictureUrl(),
                    entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map PetEntity to Domain", e);
        }
    }
}

package com.clyvo.veterinary.veterinarian.infrastructure.persistence.mapper;

import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import com.clyvo.veterinary.veterinarian.infrastructure.persistence.entity.VeterinarianEntity;
import org.springframework.stereotype.Component;

@Component
public class VeterinarianEntityMapper {

    public VeterinarianEntity toEntity(Veterinarian domain) {
        if (domain == null) return null;
        return VeterinarianEntity.builder()
            .id(domain.getId())
            .userId(domain.getUserId())
            .crm(domain.getCrm())
            .specialty(domain.getSpecialty())
            .bio(domain.getBio())
            .phone(domain.getPhone())
            .profilePictureUrl(domain.getProfilePictureUrl())
            .subscriptionPlan(domain.getSubscriptionPlan())
            .subscriptionStatus(domain.getSubscriptionStatus())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    public Veterinarian toDomain(VeterinarianEntity entity) {
        if (entity == null) return null;
        return new Veterinarian(
            entity.getId(),
            entity.getUserId(),
            entity.getCrm(),
            entity.getSpecialty(),
            entity.getBio(),
            entity.getPhone(),
            entity.getProfilePictureUrl(),
            entity.getSubscriptionPlan(),
            entity.getSubscriptionStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

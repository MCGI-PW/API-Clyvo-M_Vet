package com.clyvo.veterinary.tutor.infrastructure.persistence.mapper;

import com.clyvo.veterinary.tutor.domain.model.Tutor;
import com.clyvo.veterinary.tutor.infrastructure.persistence.entity.TutorEntity;
import org.springframework.stereotype.Component;

@Component
public class TutorEntityMapper {

    public TutorEntity toEntity(Tutor domain) {
        if (domain == null) return null;
        return TutorEntity.builder()
            .id(domain.getId())
            .userId(domain.getUserId())
            .phone(domain.getPhone())
            .address(domain.getAddress())
            .document(domain.getDocument())
            .profilePictureUrl(domain.getProfilePictureUrl())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }

    public Tutor toDomain(TutorEntity entity) {
        if (entity == null) return null;
        return new Tutor(
            entity.getId(),
            entity.getUserId(),
            entity.getPhone(),
            entity.getAddress(),
            entity.getDocument(),
            entity.getProfilePictureUrl(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}

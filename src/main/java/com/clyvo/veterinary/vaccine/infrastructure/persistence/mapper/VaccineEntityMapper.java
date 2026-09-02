package com.clyvo.veterinary.vaccine.infrastructure.persistence.mapper;

import com.clyvo.veterinary.vaccine.domain.model.Vaccine;
import com.clyvo.veterinary.vaccine.infrastructure.persistence.entity.VaccineEntity;
import org.springframework.stereotype.Component;

@Component
public class VaccineEntityMapper {

    public VaccineEntity toEntity(Vaccine domain) {
        if (domain == null) return null;
        
        return VaccineEntity.builder()
                .id(domain.getId())
                .petId(domain.getPetId())
                .vaccineName(domain.getVaccineName())
                .manufacturer(domain.getManufacturer())
                .batchNumber(domain.getBatchNumber())
                .appliedAt(domain.getAppliedAt())
                .nextDoseAt(domain.getNextDoseAt())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public Vaccine toDomain(VaccineEntity entity) {
        if (entity == null) return null;
        
        return Vaccine.load(
                entity.getId(),
                entity.getPetId(),
                entity.getVaccineName(),
                entity.getManufacturer(),
                entity.getBatchNumber(),
                entity.getAppliedAt(),
                entity.getNextDoseAt(),
                entity.getNotes(),
                entity.getCreatedAt()
        );
    }
}

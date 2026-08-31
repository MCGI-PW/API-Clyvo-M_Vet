package com.clyvo.veterinary.prescription.infrastructure.persistence.mapper;

import com.clyvo.veterinary.prescription.domain.model.Prescription;
import com.clyvo.veterinary.prescription.domain.model.PrescriptionItem;
import com.clyvo.veterinary.prescription.infrastructure.persistence.entity.PrescriptionEntity;
import com.clyvo.veterinary.prescription.infrastructure.persistence.entity.PrescriptionItemEmbeddable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrescriptionEntityMapper {

    public PrescriptionEntity toEntity(Prescription domain) {
        if (domain == null) return null;
        
        List<PrescriptionItemEmbeddable> items = domain.getMedications().stream()
                .map(i -> new PrescriptionItemEmbeddable(
                        i.medicationName(), i.dosage(), i.frequency(), i.duration(), i.instructions()))
                .collect(Collectors.toList());
                
        return PrescriptionEntity.builder()
                .id(domain.getId())
                .medicalRecordId(domain.getMedicalRecordId())
                .petId(domain.getPetId())
                .veterinarianId(domain.getVeterinarianId())
                .generalInstructions(domain.getGeneralInstructions())
                .validUntil(domain.getValidUntil())
                .createdAt(domain.getCreatedAt())
                .medications(items)
                .build();
    }

    public Prescription toDomain(PrescriptionEntity entity) {
        if (entity == null) return null;
        
        List<PrescriptionItem> items = entity.getMedications().stream()
                .map(i -> new PrescriptionItem(
                        i.getMedicationName(), i.getDosage(), i.getFrequency(), i.getDuration(), i.getInstructions()))
                .collect(Collectors.toList());
                
        return Prescription.load(
                entity.getId(),
                entity.getMedicalRecordId(),
                entity.getPetId(),
                entity.getVeterinarianId(),
                items,
                entity.getGeneralInstructions(),
                entity.getValidUntil(),
                entity.getCreatedAt()
        );
    }
}

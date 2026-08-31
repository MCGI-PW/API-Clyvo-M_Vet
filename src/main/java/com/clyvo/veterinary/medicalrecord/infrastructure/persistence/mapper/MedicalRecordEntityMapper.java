package com.clyvo.veterinary.medicalrecord.infrastructure.persistence.mapper;

import com.clyvo.veterinary.medicalrecord.domain.model.MedicalRecord;
import com.clyvo.veterinary.medicalrecord.infrastructure.persistence.entity.MedicalRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordEntityMapper {

    public MedicalRecordEntity toEntity(MedicalRecord record) {
        if (record == null) return null;
        return MedicalRecordEntity.builder()
                .id(record.getId())
                .appointmentId(record.getAppointmentId())
                .petId(record.getPetId())
                .veterinarianId(record.getVeterinarianId())
                .symptoms(record.getSymptoms())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .observations(record.getObservations())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    public MedicalRecord toDomain(MedicalRecordEntity entity) {
        if (entity == null) return null;
        try {
            java.lang.reflect.Constructor<MedicalRecord> constructor = MedicalRecord.class.getDeclaredConstructor(
                    java.util.UUID.class, java.util.UUID.class, java.util.UUID.class, java.util.UUID.class,
                    String.class, String.class, String.class, String.class,
                    java.time.LocalDateTime.class, java.time.LocalDateTime.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    entity.getId(), entity.getAppointmentId(), entity.getPetId(), entity.getVeterinarianId(),
                    entity.getSymptoms(), entity.getDiagnosis(), entity.getTreatment(), entity.getObservations(),
                    entity.getCreatedAt(), entity.getUpdatedAt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to map MedicalRecordEntity to Domain", e);
        }
    }
}

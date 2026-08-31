package com.clyvo.veterinary.medicalrecord.application.mapper;

import com.clyvo.veterinary.medicalrecord.application.dto.MedicalRecordResponse;
import com.clyvo.veterinary.medicalrecord.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordDtoMapper {

    public MedicalRecordResponse toResponse(MedicalRecord record, Object pet, Object vet, Object vetUser) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getAppointmentId(),
                record.getPetId(),
                "Pet Name",
                record.getVeterinarianId(),
                "Veterinarian Name",
                record.getSymptoms(),
                record.getDiagnosis(),
                record.getTreatment(),
                record.getObservations(),
                record.getCreatedAt()
        );
    }
}

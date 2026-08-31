package com.clyvo.veterinary.medicalrecord.application.port.in;

import com.clyvo.veterinary.medicalrecord.application.dto.CreateMedicalRecordRequest;
import com.clyvo.veterinary.medicalrecord.application.dto.MedicalRecordResponse;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordUseCase {
    MedicalRecordResponse createRecord(UUID veterinarianId, CreateMedicalRecordRequest request);
    MedicalRecordResponse updateRecord(UUID id, String observations, String treatment);
    MedicalRecordResponse getRecord(UUID id);
    List<MedicalRecordResponse> listByPet(UUID petId);
    List<MedicalRecordResponse> listByVeterinarian(UUID vetId);
    MedicalRecordResponse getByAppointment(UUID appointmentId);
}

package com.clyvo.veterinary.prescription.application.port.in;

import com.clyvo.veterinary.prescription.application.dto.CreatePrescriptionRequest;
import com.clyvo.veterinary.prescription.application.dto.PrescriptionResponse;

import java.util.List;
import java.util.UUID;

public interface PrescriptionUseCase {
    PrescriptionResponse createPrescription(UUID veterinarianId, CreatePrescriptionRequest request);
    PrescriptionResponse getPrescription(UUID id);
    List<PrescriptionResponse> listByPet(UUID petId);
    List<PrescriptionResponse> listByVeterinarian(UUID veterinarianId);
    PrescriptionResponse getByMedicalRecord(UUID medicalRecordId);
    void deletePrescription(UUID id);
}

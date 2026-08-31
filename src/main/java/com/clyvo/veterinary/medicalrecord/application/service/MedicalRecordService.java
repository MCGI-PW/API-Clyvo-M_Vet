package com.clyvo.veterinary.medicalrecord.application.service;

import com.clyvo.veterinary.medicalrecord.application.dto.CreateMedicalRecordRequest;
import com.clyvo.veterinary.medicalrecord.application.dto.MedicalRecordResponse;
import com.clyvo.veterinary.medicalrecord.application.mapper.MedicalRecordDtoMapper;
import com.clyvo.veterinary.medicalrecord.application.port.in.MedicalRecordUseCase;
import com.clyvo.veterinary.medicalrecord.domain.model.MedicalRecord;
import com.clyvo.veterinary.medicalrecord.domain.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService implements MedicalRecordUseCase {

    private final MedicalRecordRepository repository;
    private final MedicalRecordDtoMapper mapper;

    public MedicalRecordService(MedicalRecordRepository repository, MedicalRecordDtoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public MedicalRecordResponse createRecord(UUID veterinarianId, CreateMedicalRecordRequest request) {
        MedicalRecord record = MedicalRecord.create(request.appointmentId(), request.petId(), veterinarianId, request.symptoms(), request.diagnosis(), request.treatment());
        if (request.observations() != null && !request.observations().isBlank()) {
            record.addObservations(request.observations());
        }
        return mapper.toResponse(repository.save(record), null, null, null);
    }

    @Override
    public MedicalRecordResponse updateRecord(UUID id, String observations, String treatment) {
        MedicalRecord record = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Record not found"));
        if (observations != null && !observations.isBlank()) {
            record.addObservations(observations);
        }
        if (treatment != null && !treatment.isBlank()) {
            record.updateTreatment(treatment);
        }
        return mapper.toResponse(repository.save(record), null, null, null);
    }

    @Override
    public MedicalRecordResponse getRecord(UUID id) {
        MedicalRecord record = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Record not found"));
        return mapper.toResponse(record, null, null, null);
    }

    @Override
    public List<MedicalRecordResponse> listByPet(UUID petId) {
        return repository.findByPetId(petId).stream().map(r -> mapper.toResponse(r, null, null, null)).collect(Collectors.toList());
    }

    @Override
    public List<MedicalRecordResponse> listByVeterinarian(UUID vetId) {
        return repository.findByVeterinarianId(vetId).stream().map(r -> mapper.toResponse(r, null, null, null)).collect(Collectors.toList());
    }

    @Override
    public MedicalRecordResponse getByAppointment(UUID appointmentId) {
        return repository.findByAppointmentId(appointmentId)
                .map(r -> mapper.toResponse(r, null, null, null))
                .orElse(null);
    }
}

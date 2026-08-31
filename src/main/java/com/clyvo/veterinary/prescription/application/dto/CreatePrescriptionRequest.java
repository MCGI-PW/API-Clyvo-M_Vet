package com.clyvo.veterinary.prescription.application.dto;

import com.clyvo.veterinary.prescription.domain.model.PrescriptionItem;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
    @NotNull(message = "O ID do registro médico é obrigatório")
    UUID medicalRecordId,
    
    @NotNull(message = "O ID do pet é obrigatório")
    UUID petId,
    
    @NotEmpty(message = "A receita deve conter pelo menos um medicamento")
    List<PrescriptionItem> medications,
    
    String generalInstructions,
    
    @Future(message = "A data de validade deve ser no futuro")
    LocalDate validUntil
) {}

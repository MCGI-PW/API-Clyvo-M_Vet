package com.clyvo.veterinary.vaccine.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

public record CreateVaccineRequest(
    @NotNull(message = "ID do pet é obrigatório")
    UUID petId,
    
    @NotBlank(message = "Nome da vacina é obrigatório")
    String vaccineName,
    
    @NotBlank(message = "Fabricante é obrigatório")
    String manufacturer,
    
    String batchNumber,
    
    @NotNull(message = "Data de aplicação é obrigatória")
    @PastOrPresent(message = "A data de aplicação não pode ser no futuro")
    LocalDate appliedAt,
    
    LocalDate nextDoseAt,
    
    String notes
) {}

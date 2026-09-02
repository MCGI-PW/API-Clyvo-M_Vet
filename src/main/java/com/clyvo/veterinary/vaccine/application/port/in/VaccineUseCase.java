package com.clyvo.veterinary.vaccine.application.port.in;

import com.clyvo.veterinary.vaccine.application.dto.CreateVaccineRequest;
import com.clyvo.veterinary.vaccine.application.dto.VaccineResponse;

import java.util.List;
import java.util.UUID;

public interface VaccineUseCase {
    VaccineResponse registerVaccine(CreateVaccineRequest request);
    VaccineResponse getVaccine(UUID id);
    List<VaccineResponse> listByPet(UUID petId);
    void deleteVaccine(UUID id);
}

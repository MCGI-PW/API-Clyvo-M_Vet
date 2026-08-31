package com.clyvo.veterinary.pet.application.port.in;

import com.clyvo.veterinary.pet.application.dto.CreatePetRequest;
import com.clyvo.veterinary.pet.application.dto.PetResponse;
import com.clyvo.veterinary.pet.application.dto.UpdatePetRequest;
import java.util.List;
import java.util.UUID;

public interface PetUseCase {
    PetResponse createPet(CreatePetRequest request);
    PetResponse updatePet(UUID id, UpdatePetRequest request);
    PetResponse getPet(UUID id);
    List<PetResponse> listPetsByTutor(UUID tutorId);
    List<PetResponse> listAll();
    void deletePet(UUID id);
}

package com.clyvo.veterinary.veterinarian.application.port.in;

import com.clyvo.veterinary.veterinarian.application.dto.CreateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.UpdateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import java.util.List;
import java.util.UUID;

public interface VeterinarianUseCase {
    VeterinarianResponse createProfile(UUID userId, CreateVeterinarianRequest request);
    VeterinarianResponse updateProfile(UUID veterinarianId, UpdateVeterinarianRequest request);
    VeterinarianResponse getProfile(UUID veterinarianId);
    VeterinarianResponse getProfileByUserId(UUID userId);
    List<VeterinarianResponse> listAll();
    void deleteProfile(UUID veterinarianId);
}

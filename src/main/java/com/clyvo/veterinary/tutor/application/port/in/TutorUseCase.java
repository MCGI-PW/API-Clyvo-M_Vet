package com.clyvo.veterinary.tutor.application.port.in;

import com.clyvo.veterinary.tutor.application.dto.CreateTutorRequest;
import com.clyvo.veterinary.tutor.application.dto.TutorResponse;
import com.clyvo.veterinary.tutor.application.dto.UpdateTutorRequest;

import java.util.List;
import java.util.UUID;

public interface TutorUseCase {
    TutorResponse createProfile(UUID userId, CreateTutorRequest request);
    TutorResponse updateProfile(UUID tutorId, UpdateTutorRequest request);
    TutorResponse getProfile(UUID tutorId);
    TutorResponse getProfileByUserId(UUID userId);
    List<TutorResponse> listAll();
    void deleteProfile(UUID tutorId);
}

package com.clyvo.veterinary.tutor.application.mapper;

import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.tutor.application.dto.TutorResponse;
import com.clyvo.veterinary.tutor.domain.model.Tutor;
import org.springframework.stereotype.Component;

@Component
public class TutorDtoMapper {

    public TutorResponse toResponse(Tutor tutor, User user) {
        if (tutor == null || user == null) {
            return null;
        }
        return new TutorResponse(
            tutor.getId(),
            tutor.getUserId(),
            user.getName(),
            user.getEmail(),
            tutor.getPhone(),
            tutor.getAddress(),
            tutor.getDocument(),
            tutor.getProfilePictureUrl(),
            tutor.getCreatedAt()
        );
    }
}

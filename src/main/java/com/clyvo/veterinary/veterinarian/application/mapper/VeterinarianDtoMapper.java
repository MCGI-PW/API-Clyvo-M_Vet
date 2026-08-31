package com.clyvo.veterinary.veterinarian.application.mapper;

import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class VeterinarianDtoMapper {

    public VeterinarianResponse toResponse(Veterinarian vet, User user) {
        if (vet == null || user == null) {
            return null;
        }
        return new VeterinarianResponse(
            vet.getId(),
            vet.getUserId(),
            user.getName(),
            user.getEmail(),
            vet.getCrm(),
            vet.getSpecialty() != null ? vet.getSpecialty().getDisplayName() : null,
            vet.getSubscriptionPlan() != null ? vet.getSubscriptionPlan().getDisplayName() : null,
            vet.getSubscriptionStatus() != null ? vet.getSubscriptionStatus().getDisplayName() : null,
            vet.getBio(),
            vet.getPhone(),
            vet.getProfilePictureUrl(),
            vet.getCreatedAt()
        );
    }

    public List<VeterinarianResponse> toResponseList(List<Veterinarian> vets, Map<UUID, User> usersById) {
        if (vets == null) {
            return List.of();
        }
        return vets.stream()
            .map(vet -> toResponse(vet, usersById.get(vet.getUserId())))
            .collect(Collectors.toList());
    }
}

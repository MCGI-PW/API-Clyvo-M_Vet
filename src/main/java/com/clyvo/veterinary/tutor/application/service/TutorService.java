package com.clyvo.veterinary.tutor.application.service;

import com.clyvo.veterinary.user.domain.model.Role;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import com.clyvo.veterinary.shared.domain.exception.BusinessException;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.tutor.application.dto.CreateTutorRequest;
import com.clyvo.veterinary.tutor.application.dto.TutorResponse;
import com.clyvo.veterinary.tutor.application.dto.UpdateTutorRequest;
import com.clyvo.veterinary.tutor.application.mapper.TutorDtoMapper;
import com.clyvo.veterinary.tutor.application.port.in.TutorUseCase;
import com.clyvo.veterinary.tutor.domain.model.Tutor;
import com.clyvo.veterinary.tutor.domain.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TutorService implements TutorUseCase {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;
    private final TutorDtoMapper dtoMapper;

    public TutorService(TutorRepository tutorRepository, UserRepository userRepository, TutorDtoMapper dtoMapper) {
        this.tutorRepository = tutorRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public TutorResponse createProfile(UUID userId, CreateTutorRequest request) {
        if (tutorRepository.existsByDocument(request.document())) {
            throw new BusinessException("Document (CPF) already registered.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != Role.ROLE_TUTOR) {
            throw new BusinessException("User does not have TUTOR role.");
        }

        if (tutorRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException("User already has a tutor profile.");
        }

        Tutor tutor = Tutor.createProfile(userId, request.phone(), request.address(), request.document());
        if (request.profilePictureUrl() != null) {
            tutor.updateProfile(tutor.getPhone(), tutor.getAddress(), request.profilePictureUrl());
        }

        tutor = tutorRepository.save(tutor);
        return dtoMapper.toResponse(tutor, user);
    }

    @Override
    public TutorResponse updateProfile(UUID tutorId, UpdateTutorRequest request) {
        Tutor tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found."));

        tutor.updateProfile(request.phone(), request.address(), request.profilePictureUrl());
        tutor = tutorRepository.save(tutor);

        User user = userRepository.findById(tutor.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        return dtoMapper.toResponse(tutor, user);
    }

    @Override
    public TutorResponse getProfile(UUID tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found."));
        User user = userRepository.findById(tutor.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return dtoMapper.toResponse(tutor, user);
    }

    @Override
    public TutorResponse getProfileByUserId(UUID userId) {
        Tutor tutor = tutorRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found for user."));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return dtoMapper.toResponse(tutor, user);
    }

    @Override
    public List<TutorResponse> listAll() {
        List<Tutor> tutors = tutorRepository.findAll();
        List<UUID> userIds = tutors.stream().map(Tutor::getUserId).toList();

        Map<UUID, User> usersById = userRepository.findAll().stream()
            .filter(u -> userIds.contains(u.getId()))
            .collect(Collectors.toMap(User::getId, u -> u));

        return tutors.stream()
            .map(t -> dtoMapper.toResponse(t, usersById.get(t.getUserId())))
            .collect(Collectors.toList());
    }

    @Override
    public void deleteProfile(UUID tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
            .orElseThrow(() -> new ResourceNotFoundException("Tutor not found."));
        tutorRepository.deleteById(tutor.getId());
    }
}

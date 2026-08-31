package com.clyvo.veterinary.veterinarian.application.service;

import com.clyvo.veterinary.user.domain.model.Role;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import com.clyvo.veterinary.shared.domain.exception.BusinessException;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.veterinarian.application.dto.CreateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.UpdateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import com.clyvo.veterinary.veterinarian.application.mapper.VeterinarianDtoMapper;
import com.clyvo.veterinary.veterinarian.application.port.in.VeterinarianUseCase;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import com.clyvo.veterinary.veterinarian.domain.repository.VeterinarianRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VeterinarianService implements VeterinarianUseCase {

    private final VeterinarianRepository veterinarianRepository;
    private final UserRepository userRepository;
    private final VeterinarianDtoMapper dtoMapper;

    public VeterinarianService(VeterinarianRepository veterinarianRepository, UserRepository userRepository, VeterinarianDtoMapper dtoMapper) {
        this.veterinarianRepository = veterinarianRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public VeterinarianResponse createProfile(UUID userId, CreateVeterinarianRequest request) {
        if (veterinarianRepository.existsByCrm(request.crm())) {
            throw new BusinessException("CRM already registered.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        if (user.getRole() != Role.ROLE_VETERINARIAN) {
            throw new BusinessException("User does not have VETERINARIAN role.");
        }
        
        if (veterinarianRepository.findByUserId(userId).isPresent()) {
            throw new BusinessException("User already has a veterinarian profile.");
        }

        Veterinarian vet = Veterinarian.createProfile(
            userId,
            request.crm(),
            request.specialty(),
            request.bio(),
            request.phone()
        );
        
        if (request.profilePictureUrl() != null) {
            vet.updateProfile(vet.getBio(), vet.getPhone(), request.profilePictureUrl(), vet.getSpecialty());
        }

        vet = veterinarianRepository.save(vet);
        return dtoMapper.toResponse(vet, user);
    }

    @Override
    public VeterinarianResponse updateProfile(UUID veterinarianId, UpdateVeterinarianRequest request) {
        Veterinarian vet = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new ResourceNotFoundException("Veterinarian not found."));

        vet.updateProfile(request.bio(), request.phone(), request.profilePictureUrl(), request.specialty());
        vet = veterinarianRepository.save(vet);
        
        User user = userRepository.findById(vet.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            
        return dtoMapper.toResponse(vet, user);
    }

    @Override
    public VeterinarianResponse getProfile(UUID veterinarianId) {
        Veterinarian vet = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new ResourceNotFoundException("Veterinarian not found."));
            
        User user = userRepository.findById(vet.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            
        return dtoMapper.toResponse(vet, user);
    }

    @Override
    public VeterinarianResponse getProfileByUserId(UUID userId) {
        Veterinarian vet = veterinarianRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Veterinarian profile not found for user."));
            
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
            
        return dtoMapper.toResponse(vet, user);
    }

    @Override
    public List<VeterinarianResponse> listAll() {
        List<Veterinarian> vets = veterinarianRepository.findAll();
        List<UUID> userIds = vets.stream().map(Veterinarian::getUserId).toList();
        
        Map<UUID, User> usersById = userRepository.findAll().stream()
            .filter(u -> userIds.contains(u.getId()))
            .collect(Collectors.toMap(User::getId, u -> u));
            
        return dtoMapper.toResponseList(vets, usersById);
    }

    @Override
    public void deleteProfile(UUID veterinarianId) {
        Veterinarian vet = veterinarianRepository.findById(veterinarianId)
            .orElseThrow(() -> new ResourceNotFoundException("Veterinarian not found."));
        veterinarianRepository.deleteById(vet.getId());
    }
}

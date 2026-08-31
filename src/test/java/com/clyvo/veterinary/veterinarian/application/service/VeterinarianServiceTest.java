package com.clyvo.veterinary.veterinarian.application.service;

import com.clyvo.veterinary.shared.domain.exception.BusinessException;
import com.clyvo.veterinary.shared.domain.exception.ResourceNotFoundException;
import com.clyvo.veterinary.user.domain.model.Role;
import com.clyvo.veterinary.user.domain.model.User;
import com.clyvo.veterinary.user.domain.repository.UserRepository;
import com.clyvo.veterinary.veterinarian.application.dto.CreateVeterinarianRequest;
import com.clyvo.veterinary.veterinarian.application.dto.VeterinarianResponse;
import com.clyvo.veterinary.veterinarian.application.mapper.VeterinarianDtoMapper;
import com.clyvo.veterinary.veterinarian.domain.model.Specialty;
import com.clyvo.veterinary.veterinarian.domain.model.Veterinarian;
import com.clyvo.veterinary.veterinarian.domain.repository.VeterinarianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeterinarianService — Testes Unitarios")
class VeterinarianServiceTest {

    @Mock
    private VeterinarianRepository veterinarianRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VeterinarianDtoMapper veterinarianDtoMapper;

    @InjectMocks
    private VeterinarianService veterinarianService;

    private UUID userId;
    private UUID veterinarianId;
    private User vetUser;
    private CreateVeterinarianRequest createRequest;
    private Veterinarian veterinarian;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        veterinarianId = UUID.randomUUID();

        vetUser = User.create("Dr. Ana Lima", "ana@clyvovet.com", "$2a$hashed", Role.ROLE_VETERINARIAN);

        createRequest = new CreateVeterinarianRequest(
                "12345-SP",
                Specialty.GENERAL_PRACTICE,
                "Especialista em pequenos animais com 10 anos de experiencia.",
                "(11) 99999-8888",
                null
        );

        veterinarian = Veterinarian.createProfile(userId, "12345-SP", Specialty.GENERAL_PRACTICE,
                "Bio de teste", "(11) 99999-8888");
    }

    @Test
    @DisplayName("createProfile — CRM disponivel e userId valido — deve criar e retornar VeterinarianResponse")
    void createProfile_crmAvailableAndValidUserId_shouldCreateAndReturnResponse() {
        when(veterinarianRepository.existsByCrm(createRequest.crm())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(vetUser));
        when(veterinarianRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(veterinarianRepository.save(any(Veterinarian.class))).thenReturn(veterinarian);
        when(veterinarianDtoMapper.toResponse(any(Veterinarian.class), any(User.class)))
                .thenReturn(buildMockResponse());

        VeterinarianResponse result = veterinarianService.createProfile(userId, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.crm()).isEqualTo("12345-SP");
        verify(veterinarianRepository, times(1)).save(any(Veterinarian.class));
    }

    @Test
    @DisplayName("createProfile — CRM ja cadastrado — deve lancar BusinessException")
    void createProfile_crmAlreadyExists_shouldThrowBusinessException() {
        when(veterinarianRepository.existsByCrm(createRequest.crm())).thenReturn(true);

        assertThatThrownBy(() -> veterinarianService.createProfile(userId, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CRM");

        verify(veterinarianRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProfile — userId inexistente — deve lancar ResourceNotFoundException")
    void createProfile_userIdNotFound_shouldThrowResourceNotFoundException() {
        when(veterinarianRepository.existsByCrm(createRequest.crm())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veterinarianService.createProfile(userId, createRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(veterinarianRepository, never()).save(any());
    }

    @Test
    @DisplayName("createProfile — user com role TUTOR — deve lancar BusinessException")
    void createProfile_userWithWrongRole_shouldThrowBusinessException() {
        User tutorUser = User.create("Jose Silva", "jose@email.com", "$2a$hashed", Role.ROLE_TUTOR);
        when(veterinarianRepository.existsByCrm(createRequest.crm())).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(tutorUser));

        assertThatThrownBy(() -> veterinarianService.createProfile(userId, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("VETERINARIAN");

        verify(veterinarianRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProfile — id existente — deve retornar VeterinarianResponse")
    void getProfile_existingId_shouldReturnResponse() {
        when(veterinarianRepository.findById(veterinarianId)).thenReturn(Optional.of(veterinarian));
        when(userRepository.findById(any())).thenReturn(Optional.of(vetUser));
        when(veterinarianDtoMapper.toResponse(any(), any())).thenReturn(buildMockResponse());

        VeterinarianResponse result = veterinarianService.getProfile(veterinarianId);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getProfile — id inexistente — deve lancar ResourceNotFoundException")
    void getProfile_nonExistingId_shouldThrowResourceNotFoundException() {
        when(veterinarianRepository.findById(veterinarianId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veterinarianService.getProfile(veterinarianId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Veterinarian");
    }

    private VeterinarianResponse buildMockResponse() {
        return new VeterinarianResponse(
                veterinarianId,
                userId,
                "Dr. Ana Lima",
                "ana@clyvovet.com",
                "12345-SP",
                "Clinica Geral",
                "Gratuito",
                "ACTIVE",
                "Bio de teste",
                "(11) 99999-8888",
                null,
                null
        );
    }
}

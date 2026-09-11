package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.Pet;
import com.clyvo.veterinary.models.Tutor;
import com.clyvo.veterinary.repositories.PetRepository;
import com.clyvo.veterinary.repositories.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários pedagógicos do PetService.
 * Padrão: Arrange, Act, Assert.
 */
@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    @InjectMocks
    private PetService petService;

    private UUID idConta;
    private Tutor mockTutor;
    private Pet mockPet;

    @BeforeEach
    void setUp() {
        idConta = UUID.randomUUID();

        mockTutor = new Tutor();
        mockTutor.setIdTutor(UUID.randomUUID());
        mockTutor.setNome("Mariana Tutora");

        mockPet = new Pet();
        mockPet.setNome("Pipoca");
        mockPet.setSexo("FEMEA");
    }

    @Test
    @DisplayName("Deve salvar o pet associando-o ao tutor autenticado")
    void deveCadastrarPetComSucesso() {
        // Arrange
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(mockTutor));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet p = invocation.getArgument(0);
            p.setIdPet(UUID.randomUUID());
            return p;
        });

        // Act
        Pet petSalvo = petService.createPet(mockPet, idConta);

        // Assert
        assertNotNull(petSalvo.getIdPet(), "O ID do pet deve ser gerado na persistência.");
        assertEquals(mockTutor, petSalvo.getTutor(), "O tutor do pet deve ser o tutor da conta logada.");
        assertTrue(petSalvo.getAtivo(), "O pet deve ser salvo como ativo.");
        verify(petRepository, times(1)).save(mockPet);
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar pet se o tutor não for encontrado")
    void deveLancarExcecaoQuandoTutorNaoEncontrado() {
        // Arrange
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            petService.createPet(mockPet, idConta);
        });

        assertEquals("Tutor não encontrado para a conta logada.", ex.getMessage());
        verify(petRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar lista de pets pertencentes ao tutor")
    void deveListarPetsDoTutor() {
        // Arrange
        when(tutorRepository.findByContaAcessoIdConta(idConta)).thenReturn(Optional.of(mockTutor));
        when(petRepository.findByTutorIdTutor(mockTutor.getIdTutor())).thenReturn(List.of(mockPet));

        // Act
        List<Pet> pets = petService.listPetsByConta(idConta);

        // Assert
        assertNotNull(pets);
        assertEquals(1, pets.size());
        assertEquals("Pipoca", pets.get(0).getNome());
        verify(petRepository, times(1)).findByTutorIdTutor(mockTutor.getIdTutor());
    }
}

package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.AutorizacaoAcessoPet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutorizacaoAcessoPetRepository extends JpaRepository<AutorizacaoAcessoPet, UUID> {
    List<AutorizacaoAcessoPet> findByPetTutorIdTutor(UUID idTutor);
    List<AutorizacaoAcessoPet> findByPetTutorIdTutorAndStatus(UUID idTutor, String status);
    List<AutorizacaoAcessoPet> findByClinicaIdClinica(UUID idClinica);
    List<AutorizacaoAcessoPet> findByClinicaIdClinicaAndStatus(UUID idClinica, String status);
    List<AutorizacaoAcessoPet> findByVeterinarioIdVeterinarioAndStatus(UUID idVeterinario, String status);
    Optional<AutorizacaoAcessoPet> findFirstByPetIdPetAndVeterinarioIdVeterinarioAndStatus(UUID idPet, UUID idVeterinario, String status);
    Optional<AutorizacaoAcessoPet> findFirstByPetIdPetAndClinicaIdClinicaAndStatus(UUID idPet, UUID idClinica, String status);
}

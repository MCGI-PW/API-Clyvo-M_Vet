package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.VeterinarioClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeterinarioClinicaRepository extends JpaRepository<VeterinarioClinica, UUID> {
    List<VeterinarioClinica> findByClinicaIdClinicaAndStatusVinculo(UUID idClinica, String statusVinculo);
    List<VeterinarioClinica> findByClinicaIdClinica(UUID idClinica);
    List<VeterinarioClinica> findByVeterinarioIdVeterinarioAndStatusVinculo(UUID idVeterinario, String statusVinculo);
    Optional<VeterinarioClinica> findByVeterinarioIdVeterinarioAndClinicaIdClinica(UUID idVeterinario, UUID idClinica);
}

package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, UUID> {
    Optional<Prontuario> findByConsultaIdConsulta(UUID idConsulta);
}

package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ClinicaRepository extends JpaRepository<Clinica, UUID> {
    Optional<Clinica> findByContaAcessoIdConta(UUID idConta);
}

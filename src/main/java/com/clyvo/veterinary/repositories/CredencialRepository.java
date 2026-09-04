package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CredencialRepository extends JpaRepository<Credencial, UUID> {
    Optional<Credencial> findByContaAcessoIdConta(UUID idConta);
}

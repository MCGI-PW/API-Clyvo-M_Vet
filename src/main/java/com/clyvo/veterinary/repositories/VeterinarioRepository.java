package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface VeterinarioRepository extends JpaRepository<Veterinario, UUID> {
    Optional<Veterinario> findByContaAcessoIdConta(UUID idConta);
}

package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.ContaAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, UUID> {
    Optional<ContaAcesso> findByEmail(String email);
}

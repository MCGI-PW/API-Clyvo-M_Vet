package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {
    Optional<Sessao> findByTokenHash(String tokenHash);
}

package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.IdentificadorAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface IdentificadorAcessoRepository extends JpaRepository<IdentificadorAcesso, UUID> {
    Optional<IdentificadorAcesso> findByTipoIdentificadorAndValorHash(String tipo, String hash);
}

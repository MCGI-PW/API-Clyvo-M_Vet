package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.Raca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RacaRepository extends JpaRepository<Raca, UUID> {
    List<Raca> findByEspecieIdEspecie(UUID idEspecie);
    Optional<Raca> findByNome(String nome);
}

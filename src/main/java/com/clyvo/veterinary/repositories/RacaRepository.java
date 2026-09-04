package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Raca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RacaRepository extends JpaRepository<Raca, UUID> {}

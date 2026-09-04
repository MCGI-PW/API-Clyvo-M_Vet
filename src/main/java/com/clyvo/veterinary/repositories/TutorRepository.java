package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TutorRepository extends JpaRepository<Tutor, UUID> {
    Optional<Tutor> findByContaAcessoIdConta(UUID idConta);
}

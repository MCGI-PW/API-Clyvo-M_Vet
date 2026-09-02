package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, UUID> {
    java.util.Optional<Veterinarian> findByUserId(java.util.UUID userId);
}

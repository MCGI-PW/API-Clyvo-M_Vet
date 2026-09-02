package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface TutorRepository extends JpaRepository<Tutor, UUID> {
    java.util.Optional<Tutor> findByUserId(java.util.UUID userId);
}

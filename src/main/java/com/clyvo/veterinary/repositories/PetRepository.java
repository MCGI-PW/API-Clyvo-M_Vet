package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {
    List<Pet> findByTutorIdTutor(UUID idTutor);
}

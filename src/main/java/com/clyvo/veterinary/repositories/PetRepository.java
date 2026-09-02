package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {
    java.util.List<Pet> findByTutorId(java.util.UUID tutorId);
}

package com.clyvo.veterinary.repositories;
import com.clyvo.veterinary.models.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EspecieRepository extends JpaRepository<Especie, UUID> {}

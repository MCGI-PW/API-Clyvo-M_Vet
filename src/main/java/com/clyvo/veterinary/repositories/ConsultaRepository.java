package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {
    List<Consulta> findByPetIdPet(UUID idPet);
    List<Consulta> findByVeterinarioIdVeterinario(UUID idVeterinario);
    List<Consulta> findByPetTutorIdTutor(UUID idTutor);
}

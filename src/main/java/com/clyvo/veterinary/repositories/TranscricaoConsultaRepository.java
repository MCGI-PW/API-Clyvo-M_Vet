package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.TranscricaoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TranscricaoConsultaRepository extends JpaRepository<TranscricaoConsulta, UUID> {
    List<TranscricaoConsulta> findByConsultaIdConsultaOrderByDataGeracaoDesc(UUID idConsulta);
}

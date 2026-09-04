package com.clyvo.veterinary.repositories;

import com.clyvo.veterinary.models.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
    List<Notificacao> findByContaAcessoIdContaOrderByDataCriacaoDesc(UUID idConta);
}

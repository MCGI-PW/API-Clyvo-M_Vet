package com.clyvo.veterinary.services;

import com.clyvo.veterinary.models.ContaAcesso;
import com.clyvo.veterinary.models.Notificacao;
import com.clyvo.veterinary.repositories.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listByConta(UUID idConta) {
        return notificacaoRepository.findByContaAcessoIdContaOrderByDataCriacaoDesc(idConta);
    }

    @Transactional
    public Notificacao enviar(UUID idConta, String mensagem) {
        Notificacao notif = new Notificacao();
        ContaAcesso conta = new ContaAcesso();
        conta.setIdConta(idConta);
        notif.setContaAcesso(conta);
        notif.setMensagem(mensagem);
        return notificacaoRepository.save(notif);
    }
}

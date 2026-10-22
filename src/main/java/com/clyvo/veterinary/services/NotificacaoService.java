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

    @Transactional(readOnly = true)
    public long contarNaoLidas(UUID idConta) {
        return notificacaoRepository.countByContaAcessoIdContaAndLidaFalse(idConta);
    }

    /** Marca como lidas todas as notificações da conta e devolve quantas foram atualizadas. */
    @Transactional
    public int marcarTodasComoLidas(UUID idConta) {
        List<Notificacao> naoLidas = notificacaoRepository.findByContaAcessoIdContaAndLidaFalse(idConta);
        naoLidas.forEach(notificacao -> notificacao.setLida(true));
        return naoLidas.size();
    }

    /**
     * Registra uma notificação na caixa de entrada da conta.
     * Ignora silenciosamente destinatários sem conta (ex.: veterinário pré-cadastrado sem login).
     */
    @Transactional
    public void notificar(ContaAcesso destinatario, String mensagem) {
        if (destinatario == null) {
            return;
        }
        Notificacao notif = new Notificacao();
        notif.setContaAcesso(destinatario);
        notif.setMensagem(mensagem);
        notificacaoRepository.save(notif);
    }
}

package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.TranscricaoConsulta;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.TranscricaoConsultaRepository;
import com.clyvo.veterinary.services.ia.AssistenteClinico;
import com.clyvo.veterinary.services.ia.AssistenteClinicoLocal;
import com.clyvo.veterinary.services.ia.ClaudeAssistenteClinico;
import com.clyvo.veterinary.services.ia.ClyvoScribeAssistenteClinico;
import com.clyvo.veterinary.services.ia.ResumoClinico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Transcrição inteligente da teleconsulta: recebe o texto falado na consulta, pede à IA um resumo
 * clínico estruturado e grava tudo vinculado à consulta. O resumo é uma sugestão: só entra no
 * prontuário quando o veterinário revisa e finaliza o atendimento.
 */
@Service
public class TranscricaoService {

    private static final Logger log = LoggerFactory.getLogger(TranscricaoService.class);
    private static final int LIMITE_COLUNA = 4000;

    private final ConsultaRepository consultaRepository;
    private final TitularidadeConsulta titularidade;
    private final TranscricaoConsultaRepository transcricaoRepository;
    private final AssistenteClinico assistenteIA;
    private final AssistenteClinico assistenteLocal = new AssistenteClinicoLocal();

    @Autowired
    public TranscricaoService(ConsultaRepository consultaRepository,
                              TitularidadeConsulta titularidade,
                              TranscricaoConsultaRepository transcricaoRepository,
                              @Value("${app.ia.clyvoscribe.url:${CLYVOSCRIBE_URL:}}") String clyvoScribeUrl,
                              @Value("${app.ia.anthropic.api-key:}") String apiKey,
                              @Value("${app.ia.model:claude-opus-5-5}") String modelo) {
        this(consultaRepository, titularidade, transcricaoRepository, escolherAssistente(clyvoScribeUrl, apiKey, modelo));
    }

    /** Sem assistente de IA configurado (chave ausente), usa apenas o assistente local. */
    TranscricaoService(ConsultaRepository consultaRepository,
                       TitularidadeConsulta titularidade,
                       TranscricaoConsultaRepository transcricaoRepository,
                       AssistenteClinico assistenteIA) {
        this.consultaRepository = consultaRepository;
        this.titularidade = titularidade;
        this.transcricaoRepository = transcricaoRepository;
        this.assistenteIA = assistenteIA;
    }

    @Transactional
    public TranscricaoConsulta gerarResumo(UUID idConsulta, UUID idConta, String transcricao) {
        if (transcricao == null || transcricao.isBlank()) {
            throw new BusinessException("A transcrição da consulta está vazia.");
        }
        Consulta consulta = buscarConsultaDoResponsavel(idConsulta, idConta,
                "Acesso negado: apenas o veterinário ou a clínica responsável podem transcrever esta consulta.");
        if (StatusConsulta.CANCELADA.matches(consulta.getStatus())) {
            throw new BusinessException("Não é possível gerar resumo de uma consulta cancelada.");
        }

        String texto = transcricao.trim();
        ResumoGerado gerado = resumirComFallback(texto, contextoDoPaciente(consulta));
        return transcricaoRepository.save(montarRegistro(consulta, texto, gerado));
    }

    @Transactional(readOnly = true)
    public List<TranscricaoConsulta> listarPorConsulta(UUID idConsulta, UUID idConta) {
        buscarConsultaDoResponsavel(idConsulta, idConta, "Acesso negado às transcrições desta consulta.");
        return transcricaoRepository.findByConsultaIdConsultaOrderByDataGeracaoDesc(idConsulta);
    }

    private Consulta buscarConsultaDoResponsavel(UUID idConsulta, UUID idConta, String mensagemAcessoNegado) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));
        if (!titularidade.isResponsavelPeloAtendimento(consulta, idConta)) {
            throw new AccessDeniedException(mensagemAcessoNegado);
        }
        return consulta;
    }

    /** A consulta não pode travar por indisponibilidade da IA externa: na falha, usa o assistente local. */
    private ResumoGerado resumirComFallback(String texto, String contexto) {
        if (assistenteIA != null) {
            try {
                return new ResumoGerado(assistenteIA.resumir(texto, contexto), assistenteIA.fonte());
            } catch (RuntimeException e) {
                log.warn("Falha ao gerar resumo com {}. Usando assistente local: {}", assistenteIA.fonte(), e.getMessage());
            }
        }
        return new ResumoGerado(assistenteLocal.resumir(texto, contexto), assistenteLocal.fonte());
    }

    private static TranscricaoConsulta montarRegistro(Consulta consulta, String texto, ResumoGerado gerado) {
        ResumoClinico resumo = gerado.resumo();
        TranscricaoConsulta registro = new TranscricaoConsulta();
        registro.setConsulta(consulta);
        registro.setTextoTranscricao(texto);
        registro.setDiagnosticoSugerido(limitar(resumo.diagnostico()));
        registro.setPrescricaoSugerida(limitar(resumo.prescricao()));
        registro.setOrientacoesSugeridas(limitar(resumo.orientacoes()));
        registro.setResumoTutor(limitar(resumo.resumoTutor()));
        registro.setSinaisAlerta(limitar(resumo.sinaisDeAlerta() == null ? null : String.join("\n", resumo.sinaisDeAlerta())));
        registro.setFonteIa(gerado.fonte());
        return registro;
    }

    private record ResumoGerado(ResumoClinico resumo, String fonte) {
    }

    /**
     * Prioridade: serviço ClyvoScribe (IA da CLYVO VET em Python), depois Claude direto, senão nenhum
     * (só o assistente local). Configurado por CLYVOSCRIBE_URL e ANTHROPIC_API_KEY.
     */
    static AssistenteClinico escolherAssistente(String clyvoScribeUrl, String apiKey, String modelo) {
        if (clyvoScribeUrl != null && !clyvoScribeUrl.isBlank()) {
            return new ClyvoScribeAssistenteClinico(clyvoScribeUrl.trim());
        }
        if (apiKey != null && !apiKey.isBlank()) {
            return new ClaudeAssistenteClinico(apiKey, modelo);
        }
        return null;
    }

    /** Só dados não identificáveis do pet vão para a IA: nome do tutor, CPF e contatos ficam de fora (LGPD). */
    private static String contextoDoPaciente(Consulta consulta) {
        if (consulta.getPet() == null) {
            return "pet";
        }
        StringBuilder sb = new StringBuilder(consulta.getPet().getNome());
        if (consulta.getPet().getRaca() != null) {
            sb.append(" (").append(consulta.getPet().getRaca().getNome()).append(")");
        }
        return sb.toString();
    }

    private static String limitar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.length() > LIMITE_COLUNA ? limpo.substring(0, LIMITE_COLUNA) : limpo;
    }
}

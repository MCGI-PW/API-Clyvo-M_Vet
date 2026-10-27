package com.clyvo.veterinary.services;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import com.clyvo.veterinary.exceptions.BusinessException;
import com.clyvo.veterinary.exceptions.ResourceNotFoundException;
import com.clyvo.veterinary.models.Consulta;
import com.clyvo.veterinary.models.TranscricaoConsulta;
import com.clyvo.veterinary.models.enums.StatusConsulta;
import com.clyvo.veterinary.repositories.ClinicaRepository;
import com.clyvo.veterinary.repositories.ConsultaRepository;
import com.clyvo.veterinary.repositories.TranscricaoConsultaRepository;
import com.clyvo.veterinary.repositories.VeterinarioRepository;
import com.clyvo.veterinary.services.ia.AssistenteClinico;
import com.clyvo.veterinary.services.ia.AssistenteClinicoLocal;
import com.clyvo.veterinary.services.ia.ClaudeAssistenteClinico;
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
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final TranscricaoConsultaRepository transcricaoRepository;
    private final AssistenteClinico assistenteIA;
    private final AssistenteClinico assistenteLocal = new AssistenteClinicoLocal();

    @Autowired
    public TranscricaoService(ConsultaRepository consultaRepository,
                              VeterinarioRepository veterinarioRepository,
                              ClinicaRepository clinicaRepository,
                              TranscricaoConsultaRepository transcricaoRepository,
                              @Value("${app.ia.anthropic.api-key:}") String apiKey,
                              @Value("${app.ia.model:claude-opus-5-5}") String modelo) {
        this(consultaRepository, veterinarioRepository, clinicaRepository, transcricaoRepository,
                apiKey == null || apiKey.isBlank() ? null : new ClaudeAssistenteClinico(apiKey, modelo));
    }

    /** Sem assistente de IA configurado (chave ausente), usa apenas o assistente local. */
    TranscricaoService(ConsultaRepository consultaRepository,
                       VeterinarioRepository veterinarioRepository,
                       ClinicaRepository clinicaRepository,
                       TranscricaoConsultaRepository transcricaoRepository,
                       AssistenteClinico assistenteIA) {
        this.consultaRepository = consultaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clinicaRepository = clinicaRepository;
        this.transcricaoRepository = transcricaoRepository;
        this.assistenteIA = assistenteIA;
    }

    @Transactional
    public TranscricaoConsulta gerarResumo(UUID idConsulta, UUID idConta, String transcricao) {
        if (transcricao == null || transcricao.isBlank()) {
            throw new BusinessException("A transcrição da consulta está vazia.");
        }
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));

        if (!isVeterinarioDaConsulta(consulta, idConta) && !isClinicaDaConsulta(consulta, idConta)) {
            throw new AccessDeniedException("Acesso negado: apenas o veterinário ou a clínica responsável podem transcrever esta consulta.");
        }
        if (StatusConsulta.CANCELADA.matches(consulta.getStatus())) {
            throw new BusinessException("Não é possível gerar resumo de uma consulta cancelada.");
        }

        String texto = transcricao.trim();
        String contexto = contextoDoPaciente(consulta);

        AssistenteClinico usado = assistenteLocal;
        ResumoClinico resumo = null;
        if (assistenteIA != null) {
            try {
                resumo = assistenteIA.resumir(texto, contexto);
                usado = assistenteIA;
            } catch (RuntimeException e) {
                // A consulta não pode travar por indisponibilidade da IA externa
                log.warn("Falha ao gerar resumo com {}. Usando assistente local: {}", assistenteIA.fonte(), e.getMessage());
            }
        }
        if (resumo == null) {
            resumo = assistenteLocal.resumir(texto, contexto);
        }

        TranscricaoConsulta registro = new TranscricaoConsulta();
        registro.setConsulta(consulta);
        registro.setTextoTranscricao(texto);
        registro.setDiagnosticoSugerido(limitar(resumo.diagnostico()));
        registro.setPrescricaoSugerida(limitar(resumo.prescricao()));
        registro.setOrientacoesSugeridas(limitar(resumo.orientacoes()));
        registro.setResumoTutor(limitar(resumo.resumoTutor()));
        registro.setSinaisAlerta(limitar(resumo.sinaisDeAlerta() == null ? null : String.join("\n", resumo.sinaisDeAlerta())));
        registro.setFonteIa(usado.fonte());
        return transcricaoRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<TranscricaoConsulta> listarPorConsulta(UUID idConsulta, UUID idConta) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com ID: " + idConsulta));
        if (!isVeterinarioDaConsulta(consulta, idConta) && !isClinicaDaConsulta(consulta, idConta)) {
            throw new AccessDeniedException("Acesso negado às transcrições desta consulta.");
        }
        return transcricaoRepository.findByConsultaIdConsultaOrderByDataGeracaoDesc(idConsulta);
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

    private boolean isVeterinarioDaConsulta(Consulta consulta, UUID idConta) {
        return consulta.getVeterinario() != null
                && veterinarioRepository.findByContaAcessoIdConta(idConta)
                    .map(v -> v.getIdVeterinario().equals(consulta.getVeterinario().getIdVeterinario()))
                    .orElse(false);
    }

    private boolean isClinicaDaConsulta(Consulta consulta, UUID idConta) {
        return consulta.getClinica() != null
                && clinicaRepository.findByContaAcessoIdConta(idConta)
                    .map(c -> c.getIdClinica().equals(consulta.getClinica().getIdClinica()))
                    .orElse(false);
    }
}

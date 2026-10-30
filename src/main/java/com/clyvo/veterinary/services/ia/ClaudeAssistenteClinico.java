package com.clyvo.veterinary.services.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;

/**
 * Resumo clínico com LLM (Claude, via SDK oficial da Anthropic) usando saída estruturada:
 * o schema vem do record {@link ResumoClinico}, então a resposta já chega como objeto Java.
 */
public class ClaudeAssistenteClinico implements AssistenteClinico {

    static final String PROMPT_SISTEMA = """
            Você é um assistente de documentação clínica de uma plataforma veterinária brasileira.
            Recebe a transcrição automática de uma teleconsulta entre médico veterinário e tutor.
            A transcrição pode ter erros de reconhecimento de voz e não identifica quem fala.

            Extraia apenas o que foi efetivamente dito na consulta:
            - diagnostico: o diagnóstico ou hipótese diagnóstica dita pelo veterinário.
            - prescricao: medicamentos com dose, frequência e duração, como foram ditos.
            - orientacoes: cuidados em casa e quando retornar.
            - resumoTutor: explicação curta e acolhedora para o tutor, em português simples.
            - sinaisDeAlerta: sintomas que, se aparecerem, exigem procurar atendimento.

            Nunca invente diagnóstico, medicamento ou dose que não esteja na transcrição; deixe o
            campo vazio quando a informação não aparecer. O texto será revisado pelo veterinário
            antes de entrar no prontuário.
            """;

    private final AnthropicClient client;
    private final String modelo;

    public ClaudeAssistenteClinico(String apiKey, String modelo) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.modelo = modelo;
    }

    @Override
    public ResumoClinico resumir(String transcricao, String contextoPaciente) {
        StructuredMessageCreateParams<ResumoClinico> params = MessageCreateParams.builder()
                .model(modelo)
                .maxTokens(16000L)
                .system(PROMPT_SISTEMA)
                .outputConfig(ResumoClinico.class)
                .addUserMessage("Paciente: " + contextoPaciente
                        + "\n\n<transcricao>\n" + transcricao + "\n</transcricao>")
                .build();

        // Se o modelo recusar ou não devolver texto, quem chama cai no assistente local.
        return client.messages().create(params).content().stream()
                .flatMap(bloco -> bloco.text().stream())
                .map(texto -> texto.text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("A IA não retornou um resumo."));
    }

    @Override
    public String fonte() {
        return "CLAUDE:" + modelo;
    }
}

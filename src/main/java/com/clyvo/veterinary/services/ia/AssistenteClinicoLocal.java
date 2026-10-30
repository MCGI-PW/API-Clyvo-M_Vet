package com.clyvo.veterinary.services.ia;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Assistente sem dependência externa: classifica cada frase da transcrição por palavras-chave
 * (diagnóstico, prescrição, orientação, alerta). Mantém a funcionalidade de pé sem chave de API,
 * nos testes automatizados e quando o serviço de LLM está indisponível.
 */
public class AssistenteClinicoLocal implements AssistenteClinico {

    private static final Pattern FIM_DE_FRASE = Pattern.compile("(?<=[.!?])\\s+|\\n+");

    private static final List<String> DIAGNOSTICO = List.of(
            "diagnostico", "suspeita", "suspeito", "parece ser", "quadro de", "trata-se", "e uma", "esta com");
    private static final List<String> PRESCRICAO = List.of(
            " mg", "ml", "comprimido", "gotas", "pomada", "prescrev", "receit", "vezes ao dia",
            "de 12 em 12", "de 8 em 8", "antibiotico", "anti-inflamatorio", "vermifugo", "dar o remedio");
    private static final List<String> ORIENTACAO = List.of(
            "retorn", "voltar", "volte", "evit", "mantenha", "manter", "repouso", "jejum", "observ",
            "limp", "dieta", "racao", "agua", "colar elizabetano");
    private static final List<String> ALERTA = List.of(
            "vomit", "sangue", "convuls", "nao comer", "parar de comer", "apatia", "febre",
            "falta de ar", "desmai", "piorar", "inchaco");

    @Override
    public ResumoClinico resumir(String transcricao, String contextoPaciente) {
        List<String> frases = FIM_DE_FRASE.splitAsStream(transcricao)
                .map(String::trim)
                .filter(f -> !f.isEmpty())
                .toList();

        List<String> diagnostico = new ArrayList<>();
        List<String> prescricao = new ArrayList<>();
        List<String> orientacoes = new ArrayList<>();
        Set<String> alertas = new LinkedHashSet<>();

        for (String frase : frases) {
            String normalizada = normalizar(frase);
            if (contem(normalizada, ALERTA)) {
                alertas.add(frase);
            }
            if (contem(normalizada, PRESCRICAO)) {
                prescricao.add(frase);
            } else if (contem(normalizada, DIAGNOSTICO)) {
                diagnostico.add(frase);
            } else if (contem(normalizada, ORIENTACAO)) {
                orientacoes.add(frase);
            }
        }

        String resumoTutor = "Consulta de " + contextoPaciente + ". "
                + (diagnostico.isEmpty() ? "" : "Avaliação: " + diagnostico.get(0) + " ")
                + (prescricao.isEmpty() ? "" : "Há medicação prescrita; siga as doses indicadas. ")
                + (orientacoes.isEmpty() ? "" : "Confira as orientações de cuidado e retorno.");

        return new ResumoClinico(
                String.join(" ", diagnostico),
                String.join(" ", prescricao),
                String.join(" ", orientacoes),
                resumoTutor.trim(),
                List.copyOf(alertas));
    }

    @Override
    public String fonte() {
        return "LOCAL";
    }

    private static boolean contem(String frase, List<String> termos) {
        return termos.stream().anyMatch(frase::contains);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}

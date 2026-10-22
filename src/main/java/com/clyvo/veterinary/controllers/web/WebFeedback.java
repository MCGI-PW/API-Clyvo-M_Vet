package com.clyvo.veterinary.controllers.web;

import com.clyvo.veterinary.exceptions.DomainException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Padrão Post-Redirect-Get das ações Web: executa a operação, registra a mensagem
 * de sucesso (chave i18n) ou o erro de negócio como flash attribute e redireciona.
 * Assim nenhuma mensagem trafega pela URL e nenhum controller repete try/catch.
 */
final class WebFeedback {

    static final String SUCCESS = "successMsg";
    static final String ERROR = "errorMsg";

    private WebFeedback() {
    }

    static String execute(RedirectAttributes redirect, String targetPath, String successKey, Runnable action) {
        try {
            action.run();
            redirect.addFlashAttribute(SUCCESS, successKey);
        } catch (DomainException e) {
            redirect.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:" + targetPath;
    }
}

package com.clyvo.veterinary.config;

import com.clyvo.veterinary.exceptions.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Ponto único para obter a conta autenticada.
 * O {@link JwtFilter} popula o SecurityContext tanto para chamadas REST (Bearer)
 * quanto para a interface Web (token guardado na sessão), então REST e Web usam este helper.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID getLoggedAccountId() {
        return findLoggedAccountId()
                .orElseThrow(() -> new AccessDeniedException("Sessão expirada ou usuário não autenticado."));
    }

    /** Versão sem exceção, para trechos que também rodam em páginas públicas (ex.: cabeçalho). */
    public static Optional<UUID> findLoggedAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof String idConta)) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(idConta));
    }
}

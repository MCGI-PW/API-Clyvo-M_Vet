package com.clyvo.veterinary.config;

import com.clyvo.veterinary.repositories.SessaoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final SessaoRepository sessaoRepository;

    public JwtFilter(JwtUtil jwtUtil, SessaoRepository sessaoRepository) {
        this.jwtUtil = jwtUtil;
        this.sessaoRepository = sessaoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtUtil.validateToken(token) && isSessaoAtiva(token)) {
            String tipoConta = jwtUtil.extractTipoConta(token);
            List<GrantedAuthority> authorities = (tipoConta == null || tipoConta.isBlank())
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + tipoConta.toUpperCase().trim()));

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(jwtUtil.extractIdConta(token), null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }

    /** Bearer token (API REST / mobile) ou token guardado na sessão HTTP (interface Web). */
    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(WebSession.TOKEN) instanceof String s && !s.isBlank()) {
            return s;
        }
        return null;
    }

    private boolean isSessaoAtiva(String token) {
        return sessaoRepository.findByTokenHash(jwtUtil.hashToken(token))
                .filter(sessao -> sessao.getDataRevogacao() == null)
                .filter(sessao -> sessao.getDataExpiracao().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}

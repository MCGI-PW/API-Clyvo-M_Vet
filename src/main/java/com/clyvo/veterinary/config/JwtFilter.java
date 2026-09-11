package com.clyvo.veterinary.config;

import com.clyvo.veterinary.models.Sessao;
import com.clyvo.veterinary.repositories.SessaoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

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
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String tokenHash = jwtUtil.hashToken(token);
                Optional<Sessao> sessaoOpt = sessaoRepository.findByTokenHash(tokenHash);

                if (sessaoOpt.isPresent()) {
                    Sessao sessao = sessaoOpt.get();
                    if (sessao.getDataRevogacao() == null && sessao.getDataExpiracao().isAfter(LocalDateTime.now())) {
                        String idConta = jwtUtil.extractIdConta(token);
                        String tipoConta = jwtUtil.extractTipoConta(token);
                        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                        if (tipoConta != null && !tipoConta.isBlank()) {
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + tipoConta.toUpperCase().trim()));
                        }
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                idConta, null, authorities
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

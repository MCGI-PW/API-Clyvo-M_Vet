package com.clyvo.veterinary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Rotas Públicas: Autenticação, Swagger OpenAPI e Recursos Estáticos
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/", "/web/login", "/web/register", "/web/logout", "/css/**", "/style.css", "/app.js", "/index.html", "/login.html").permitAll()

                // Proteção de Rotas Web com base no Perfil do Usuário (Spring Security RBAC)
                .requestMatchers("/web/tutor/**").hasRole("TUTOR")
                .requestMatchers("/web/vet/**").hasRole("VETERINARIO")
                .requestMatchers("/web/clinica/**").hasRole("CLINICA")

                // Proteção de Rotas REST com base no Perfil do Usuário
                .requestMatchers("/api/appointments/**/complete").hasAnyRole("VETERINARIO", "CLINICA")
                .requestMatchers("/api/clinicas/veterinarios/**", "/api/clinicas/autorizacoes/*/transferir").hasRole("CLINICA")
                .requestMatchers("/api/pets/**").hasAnyRole("TUTOR", "VETERINARIO", "CLINICA")
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/web/")) {
                        response.sendRedirect("/web/login?denied=true");
                    } else {
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Acesso negado: perfil não autorizado para esta funcionalidade.\"}");
                    }
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/web/")) {
                        response.sendRedirect("/web/login?unauthenticated=true");
                    } else {
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Não autenticado. Forneça um token Bearer válido.\"}");
                    }
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

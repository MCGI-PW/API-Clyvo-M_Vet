package com.clyvo.veterinary.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String WEB_PREFIX = "/web/";

    private final JwtFilter jwtFilter;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    public SecurityConfig(JwtFilter jwtFilter, @Lazy CustomOAuth2SuccessHandler customOAuth2SuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.customOAuth2SuccessHandler = customOAuth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF ativo para a interface Web (sessão + cookies). A API REST é stateless (Bearer token),
        // portanto não é vulnerável a CSRF e fica fora da verificação para manter o contrato com o app mobile.
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Rotas Públicas: Autenticação, OAuth2, Swagger OpenAPI e Recursos Estáticos
                .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/", "/web/login", "/web/register", "/web/logout", "/web/login/oauth2-mock",
                        "/oauth2/**", "/login/oauth2/**", "/style.css", "/app.js").permitAll()

                // Proteção de Rotas Web com base no Perfil do Usuário (Spring Security RBAC)
                .requestMatchers("/web/tutor/**").hasRole("TUTOR")
                .requestMatchers("/web/vet/**").hasRole("VETERINARIO")
                .requestMatchers("/web/clinica/**").hasRole("CLINICA")
                .requestMatchers("/web/notificacoes/**").authenticated()

                // Proteção de Rotas REST com base no Perfil do Usuário
                .requestMatchers("/api/appointments/*/complete", "/api/appointments/*/ai-summary").hasAnyRole("VETERINARIO", "CLINICA")
                .requestMatchers("/api/clinicas/veterinarios/**", "/api/clinicas/autorizacoes/*/transferir").hasRole("CLINICA")
                .requestMatchers("/api/tutors/**").hasAnyRole("VETERINARIO", "CLINICA")
                .requestMatchers("/api/pets/**").hasAnyRole("TUTOR", "VETERINARIO", "CLINICA")
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/web/login")
                .successHandler(customOAuth2SuccessHandler)
                .failureUrl("/web/login?error=oauth2")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, e) ->
                        respond(request, response, HttpServletResponse.SC_FORBIDDEN, "denied",
                                "Acesso negado: perfil não autorizado para esta funcionalidade."))
                .authenticationEntryPoint((request, response, e) ->
                        respond(request, response, HttpServletResponse.SC_UNAUTHORIZED, "unauthenticated",
                                "Não autenticado. Forneça um token Bearer válido."))
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Páginas Web voltam ao login com aviso; a API REST responde JSON com o status HTTP adequado. */
    private static void respond(HttpServletRequest request, HttpServletResponse response,
                                int status, String loginFlag, String message) throws IOException {
        if (request.getRequestURI().startsWith(WEB_PREFIX)) {
            response.sendRedirect("/web/login?" + loginFlag + "=true");
            return;
        }
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

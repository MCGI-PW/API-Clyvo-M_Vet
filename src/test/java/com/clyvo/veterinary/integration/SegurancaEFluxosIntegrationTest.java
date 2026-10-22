package com.clyvo.veterinary.integration;

import com.clyvo.veterinary.config.WebSession;
import com.clyvo.veterinary.repositories.ContaAcessoRepository;
import com.clyvo.veterinary.services.NotificacaoService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração ponta a ponta (contexto Spring completo + H2 + MockMvc):
 * cadeia de filtros do Spring Security, CSRF, RBAC, redirecionamentos Web,
 * validação dos DTOs da API REST e a caixa de notificações.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaEFluxosIntegrationTest {

    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ContaAcessoRepository contaRepository;

    // ---------------------------------------------------------------- helpers

    private static String novoEmail(String prefixo) {
        return prefixo + "." + UUID.randomUUID() + "@teste.com";
    }

    /** Cadastra uma conta pela API REST e devolve o JWT (documento e CRMV únicos por conta). */
    private String registrar(String email, String perfil) throws Exception {
        String unico = UUID.randomUUID().toString().substring(0, 8);
        String body = """
                {"name": "Usuário Teste", "email": "%s", "password": "%s", "role": "%s",
                 "document": "doc-%s", "crmv": "CRMV-SP %s"}
                """.formatted(email, SENHA, perfil, unico, unico);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    /** Faz login pelo formulário Web e devolve a sessão HTTP autenticada. */
    private MockHttpSession loginWeb(String email) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/web/login").session(session).with(csrf())
                        .param("email", email).param("password", SENHA))
                .andExpect(status().is3xxRedirection());
        return session;
    }

    // ------------------------------------------------------------ segurança

    @Nested
    @DisplayName("Segurança (Spring Security)")
    class Seguranca {

        @Test
        @DisplayName("API REST sem token responde 401 em JSON")
        void apiSemTokenRetorna401() throws Exception {
            mockMvc.perform(get("/api/pets"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error", notNullValue()));
        }

        @Test
        @DisplayName("Página Web protegida sem sessão redireciona ao login com aviso")
        void paginaWebSemSessaoRedirecionaAoLogin() throws Exception {
            mockMvc.perform(get("/web/tutor/dashboard"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/login?unauthenticated=true"));
        }

        @Test
        @DisplayName("Formulário Web sem token CSRF é bloqueado e volta ao login com aviso")
        void formularioWebSemCsrfEhBloqueado() throws Exception {
            String email = novoEmail("tutor");
            registrar(email, "TUTOR");
            MockHttpSession session = new MockHttpSession();

            mockMvc.perform(post("/web/login").session(session).param("email", email).param("password", SENHA))
                    .andExpect(redirectedUrl("/web/login?denied=true"));
            assertNull(session.getAttribute(WebSession.TOKEN));
        }

        @Test
        @DisplayName("Tutor não pode concluir atendimento (RBAC na API REST)")
        void tutorNaoConcluiAtendimento() throws Exception {
            String token = registrar(novoEmail("tutor"), "TUTOR");

            mockMvc.perform(post("/api/appointments/{id}/complete", UUID.randomUUID())
                            .header("Authorization", bearer(token)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Veterinário logado não abre o painel do tutor (RBAC na Web)")
        void veterinarioNaoAbrePainelDoTutor() throws Exception {
            String email = novoEmail("vet");
            registrar(email, "VETERINARIO");
            MockHttpSession session = loginWeb(email);

            mockMvc.perform(get("/web/tutor/dashboard").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/login?denied=true"));
        }

        @Test
        @DisplayName("Após o logout, a sessão JWT é revogada e o painel volta a exigir login")
        void logoutRevogaSessao() throws Exception {
            String email = novoEmail("tutor");
            registrar(email, "TUTOR");
            MockHttpSession session = loginWeb(email);
            String token = (String) session.getAttribute(WebSession.TOKEN);

            mockMvc.perform(get("/web/logout").session(session))
                    .andExpect(redirectedUrl("/web/login?logout=true"));

            mockMvc.perform(get("/api/pets").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("Dois logins seguidos da mesma conta geram sessões distintas e válidas")
    void loginsSeguidosGeramTokensDistintos() throws Exception {
        String email = novoEmail("tutor");
        String tokenCadastro = registrar(email, "TUTOR");
        MockHttpSession web = loginWeb(email);

        mockMvc.perform(get("/web/tutor/dashboard").session(web)).andExpect(status().isOk());
        mockMvc.perform(get("/api/pets").header("Authorization", bearer(tokenCadastro))).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cadastro com CPF já usado responde 400 com mensagem clara (sem erro 500)")
    void cadastroComDocumentoDuplicadoRetorna400() throws Exception {
        String body = """
                {"name": "Usuário", "email": "%s", "password": "%s", "role": "TUTOR", "document": "99988877766"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(novoEmail("a"), SENHA)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(novoEmail("b"), SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CPF já cadastrado no sistema."));
    }

    // ------------------------------------------------------------ fluxos Web

    @Nested
    @DisplayName("Fluxos da interface Web")
    class FluxosWeb {

        @Test
        @DisplayName("Login Web redireciona cada perfil ao próprio painel")
        void loginRedirecionaPorPerfil() throws Exception {
            String tutor = novoEmail("tutor");
            String clinica = novoEmail("clinica");
            registrar(tutor, "TUTOR");
            registrar(clinica, "CLINICA");

            mockMvc.perform(post("/web/login").with(csrf()).param("email", tutor).param("password", SENHA))
                    .andExpect(redirectedUrl("/web/tutor/dashboard"));
            mockMvc.perform(post("/web/login").with(csrf()).param("email", clinica).param("password", SENHA))
                    .andExpect(redirectedUrl("/web/clinica/dashboard"));
        }

        @Test
        @DisplayName("Senha errada mantém o usuário na tela de login com mensagem de erro")
        void senhaErradaMostraErro() throws Exception {
            String email = novoEmail("tutor");
            registrar(email, "TUTOR");

            mockMvc.perform(post("/web/login").with(csrf()).param("email", email).param("password", "errada"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("web/login"))
                    .andExpect(model().attributeExists("errorMsg"));
        }

        @Test
        @DisplayName("Tutor cadastra pet pela Web (PRG com mensagem flash) e o vê no painel")
        void tutorCadastraPetPelaWeb() throws Exception {
            String email = novoEmail("tutor");
            registrar(email, "TUTOR");
            MockHttpSession session = loginWeb(email);

            mockMvc.perform(post("/web/tutor/pets").session(session).with(csrf())
                            .param("nome", "Paçoca").param("sexo", "F"))
                    .andExpect(redirectedUrl("/web/tutor/dashboard"))
                    .andExpect(flash().attribute("successMsg", "msg.pet.created"));

            mockMvc.perform(get("/web/tutor/dashboard").session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Paçoca")));
        }

        @Test
        @DisplayName("Caixa de notificações mostra o contador e marca todas como lidas")
        void caixaDeNotificacoes() throws Exception {
            String email = novoEmail("tutor");
            registrar(email, "TUTOR");
            notificacaoService.notificar(contaRepository.findByEmail(email).orElseThrow(), "Prontuário do Thor disponível.");
            MockHttpSession session = loginWeb(email);

            mockMvc.perform(get("/web/notificacoes").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("web/notificacoes"))
                    .andExpect(model().attribute("dashboardPath", "/web/tutor/dashboard"))
                    .andExpect(model().attribute("notificacoesNaoLidas", 1L))
                    .andExpect(content().string(containsString("Prontuário do Thor disponível.")));

            mockMvc.perform(post("/web/notificacoes/lidas").session(session).with(csrf()))
                    .andExpect(redirectedUrl("/web/notificacoes"))
                    .andExpect(flash().attribute("successMsg", "msg.notificacoes.read"));

            mockMvc.perform(get("/web/notificacoes").session(session))
                    .andExpect(model().attribute("notificacoesNaoLidas", 0L));
        }
    }

    // ------------------------------------------------------------ API REST

    @Nested
    @DisplayName("API REST (DTOs e validação)")
    class ApiRest {

        @Test
        @DisplayName("POST /api/pets valida o DTO e responde 400 com o campo inválido")
        void petSemNomeRetorna400() throws Exception {
            String token = registrar(novoEmail("tutor"), "TUTOR");

            mockMvc.perform(post("/api/pets").header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.nome").value("O nome do pet é obrigatório."));
        }

        @Test
        @DisplayName("POST /api/pets ignora campos controlados pelo servidor (id e status)")
        void petIgnoraCamposDoServidor() throws Exception {
            String token = registrar(novoEmail("tutor"), "TUTOR");
            UUID idForjado = UUID.randomUUID();
            String body = """
                    {"idPet": "%s", "nome": "Thor", "sexo": "M", "ativo": false}
                    """.formatted(idForjado);

            mockMvc.perform(post("/api/pets").header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Thor"))
                    .andExpect(jsonPath("$.ativo").value(true))
                    .andExpect(jsonPath("$.idPet").value(not(idForjado.toString())));

            mockMvc.perform(get("/api/pets").header("Authorization", bearer(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nome").value("Thor"));
        }

        @Test
        @DisplayName("POST /api/consultas recusa modalidade inválida e campos obrigatórios ausentes")
        void consultaInvalidaRetorna400() throws Exception {
            String token = registrar(novoEmail("tutor"), "TUTOR");
            String body = """
                    {"pet": {"idPet": "%s"}, "modalidade": "ONLINE"}
                    """.formatted(UUID.randomUUID());

            mockMvc.perform(post("/api/consultas").header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.modalidade").value("A modalidade deve ser PRESENCIAL ou TELEMEDICINA."))
                    .andExpect(jsonPath("$.veterinario").value("Informe o veterinário."))
                    .andExpect(jsonPath("$.dataHora").value("Informe a data e a hora da consulta."));
        }

        @Test
        @DisplayName("Notificações: contador de não lidas e marcação como lidas pela API")
        void notificacoesPelaApi() throws Exception {
            String token = registrar(novoEmail("tutor"), "TUTOR");

            mockMvc.perform(get("/api/notificacoes/nao-lidas").header("Authorization", bearer(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.naoLidas").value(0));
            mockMvc.perform(put("/api/notificacoes/lidas").header("Authorization", bearer(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.atualizadas").value(0));
        }
    }
}

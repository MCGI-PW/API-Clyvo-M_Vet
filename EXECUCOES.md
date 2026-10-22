# 🛠️ Execuções: Revisão e Correções do Clyvo Vet

> Registro de tudo o que foi analisado e corrigido no repositório **API-Clyvo-M_Vet** para a entrega final, e do que **ainda falta o grupo fazer**.
>
> 📅 **Data:** 22/10/2026 · 🌿 **Branch:** `main` (alterações **ainda não commitadas**, veja a [seção 9](#-9-o-que-falta-fazer))

---

## 📊 Resumo em números

| Indicador | Antes | Depois |
|---|:---:|:---:|
| ✅ Testes automatizados | 28 | **51** (todos passando, incluindo 15 de integração) |
| 🎨 Estilos inline (`style="..."`) nos templates | 216 | **0** |
| 🌐 Textos traduzidos (PT/EN) | 27 | **todas as telas** |
| 🔁 Trechos de código duplicados | ~35 | **0 conhecidos** |
| 🧹 Código morto removido | — | **~1.300 linhas** (`app.js` antigo + `css/main.css`) |
| 🐞 Bugs que quebravam a demonstração | 3 | **0** |
| 📁 Arquivos alterados | — | **51** (+2.374 / −3.085 linhas) |

### Status geral

| Área | Status |
|---|:---:|
| Bugs de fluxo | 🟢 Corrigidos |
| Boas práticas e duplicação de código | 🟢 Corrigidas |
| Segurança e LGPD | 🟢 Reforçadas |
| UI/UX e i18n | 🟢 Refeitos |
| Testes | 🟢 51/51 |
| Repositório DevOps | 🟢 Alinhado ao Oracle, sem senhas fixas |
| README | 🟡 Reescrito, faltam dados do grupo |
| Deploy online | 🔴 **Pendente (grupo)** |
| Evidência de colaboração | 🔴 **Pendente (grupo)** |
| Vídeo | 🔴 **Pendente (grupo)** |

---

## 📌 Sumário

1. [Bugs corrigidos](#-1-bugs-corrigidos)
2. [Código duplicado eliminado](#-2-código-duplicado-eliminado)
3. [Boas práticas](#-3-boas-práticas)
4. [Segurança e LGPD](#-4-segurança-e-lgpd)
5. [Interface (UI/UX) e idiomas](#-5-interface-uiux-e-idiomas)
6. [Configuração e execução](#-6-configuração-e-execução)
7. [Testes](#-7-testes)
8. [Como tudo foi verificado](#-8-como-tudo-foi-verificado)
9. [O que falta fazer](#-9-o-que-falta-fazer) ⬅️ **checklist do grupo**
10. [Mapa de arquivos alterados](#-10-mapa-de-arquivos-alterados)

---

## 🐞 1. Bugs corrigidos

Estes problemas **apareceriam durante a apresentação** (penalidade de −5 por erro visual ou de fluxo).

| # | Problema | Impacto | Correção |
|:-:|---|---|---|
| 1 | O formulário de **transferência de atendimento** da clínica enviava para `/web/clinica/autorizacoes/transferir`, mas o controller só tinha `/autorizacoes/{id}/transferir` | ❌ Fluxo da clínica quebrava com erro | Nova rota que recebe o id pelo formulário, em `WebClinicaController` |
| 2 | A transferência listava **todos** os veterinários, inclusive os sem vínculo com a clínica | ❌ Escolher um deles gerava erro | O select agora lista só a **equipe ativa** (`equipeAtiva`) |
| 3 | As classes `.btn-cancel`, `.status-badge`, `.form-input`, `.form-group` e `.form-label` **não existiam** no CSS, e a variável `--accent` também não | ❌ "Cancelar" tinha a mesma aparência de "Confirmar"; borda do prontuário sumia | Todas criadas no `style.css`; ações destrutivas agora são vermelhas |
| 4 | Mensagens de erro iam **pela URL** (`?error=Acesso negado: ...`), sem codificação | ⚠️ URL feia; acentos e espaços podiam quebrar o redirecionamento | Padrão **Post-Redirect-Get** com *flash attributes* (`WebFeedback`) |
| 5 | `app.js` tinha ~730 linhas de uma versão SPA antiga apontando para `http://localhost:8080/api` | ⚠️ Código morto, que quebraria online | Removido; ficou só o tema claro/escuro e as confirmações |
| 6 | `application-local.properties` e `application-prod.properties` estavam no `.gitignore` | ❌ O projeto **não rodava após o clone**, e o Docker montava o JAR **sem o perfil `prod`** | Liberados no `.gitignore` (eles não têm segredos, só `${VARIAVEIS}`) |
| 7 | A tela de login não tratava `?denied=true` nem `?unauthenticated=true` | ⚠️ Redirecionamento sem nenhuma explicação para o usuário | Mensagens de "acesso negado" e "sessão expirada" |
| 8 | Era possível **concluir uma consulta já cancelada** | ⚠️ Regra de negócio incoerente | `ConsultaService.completeConsulta` aceita apenas consultas `AGENDADO` |

---

## 🔁 2. Código duplicado eliminado

Cada repetição custa **−5 pontos**. Tudo foi extraído para métodos ou classes reutilizáveis:

| Repetição encontrada | Onde estava | Cópias | Agora |
|---|---|:-:|---|
| `getLoggedAccountId()` | `ConsultaController`, `PetController`, `AppointmentController`, `NotificacaoController`, `AutorizacaoController`, `ClinicaController` | 6 | 🆕 `config/SecurityUtils` |
| `getAccountIdFromSession()` | `WebTutorController`, `WebVetController`, `WebClinicaController` | 3 | Também usa `SecurityUtils` (o `JwtFilter` já autentica a sessão Web) |
| "Se o perfil for X, redireciona para o painel Y" + gravação na sessão | `WebAuthController` (3×) e `CustomOAuth2SuccessHandler` | 4 | 🆕 `config/WebSession` + rota do painel dentro do enum `TipoConta` |
| `if (clinica vazia) return 403` | `ClinicaController` | 7 | `ClinicaService.getClinicaByConta()` lança `AccessDeniedException` |
| `new Notificacao(); setConta...; save` | `ConsultaService`, `AutorizacaoService`, `ClinicaService` | 10 | `NotificacaoService.notificar(conta, mensagem)` |
| `seedTutor` / `seedClinica` / `seedVeterinario` quase idênticos | `DataInitializer` | 3 | Um único `seedConta(...)`, que reutiliza `AuthService.criarConta` |
| Criação de conta + credencial | `AuthService.register` e `loginOrRegisterOAuth2` | 2 | `criarContaComCredencial()` e `criarPerfil()` |
| `try { ... } catch (Exception e) { redirect ?error= }` | Todos os controllers Web | 9 | 🆕 `controllers/web/WebFeedback.execute(...)` |
| `catch` que comparava o **texto** da mensagem (`contains("não encontrada")`) | `ConsultaController`, `AutorizacaoController` | 2 | Removido: quem trata é o `GlobalExceptionHandler` |
| `Jwts.parser().verifyWith(...)...` | `JwtUtil` | 4 | `parseClaims()` privado |
| Cabeçalho, alertas, badge de status e lista de prontuários | 4 templates | 4 | 🆕 `templates/fragments/layout.html` |
| Ternário gigante de cor do status (`th:style`) | 3 templates | 3 | Fragmento `status(value)` + classes CSS `.status-*` |

---

## 🧱 3. Boas práticas

Cada violação custa **−10 pontos**.

| Antes ❌ | Depois ✅ |
|---|---|
| Os enums `StatusConsulta`, `TipoConta` etc. existiam, mas o código usava strings soltas (`"ATIVA"`, `"AGENDADO"`, `"CANCELADA"`...) | Enums usados em todo o código, com o helper `matches()` (ex.: `StatusConsulta.CONCLUIDA.matches(status)`) |
| `throw new RuntimeException("Clínica não encontrada.")` | Hierarquia de domínio: 🆕 `DomainException` → `BusinessException` (400), `AccessDeniedException` (403), `ResourceNotFoundException` (404), `AccountBlockedException` (403) |
| `catch (Exception e)` em todos os controllers Web | Captura apenas `DomainException` e `BadCredentialsException` |
| `createConsulta` com ~110 linhas | Dividido em `validarPetDoTutor`, `validarDataFutura`, `buscarVeterinario`, `resolverClinicaDoVeterinario`, `validarAgendaLivre` e `garantirAutorizacaoDeAcesso` |
| Nomes totalmente qualificados no meio do código (`com.clyvo...exceptions.X`, `java.util.List`) | Imports normais |
| Métodos sobrecarregados em `ClinicaService` que buscavam a clínica **pelo id da conta** como "plano B" | Removidos; a clínica vem sempre da conta autenticada |
| `catch (Exception)` genérico no `JwtUtil` | Captura `JwtException` e `IllegalArgumentException` |
| Mensagens de validação sem acento ("O email e obrigatorio") | Corrigidas ("O e-mail é obrigatório.") |
| Números mágicos (5 tentativas, 15 minutos) | Constantes `MAX_TENTATIVAS` e `MINUTOS_BLOQUEIO` |

> ℹ️ **Decisão consciente:** a API REST continua recebendo as entidades `Pet` e `Consulta` no `@RequestBody`, sem DTOs. Isso preserva o formato JSON que o **aplicativo mobile** já envia. Se o professor perguntar, essa é a justificativa.

---

## 🔒 4. Segurança e LGPD

| Melhoria | Arquivo |
|---|---|
| **CSRF ativado** nas telas Web; a API REST (stateless, Bearer) continua fora dessa verificação | `SecurityConfig` |
| **Logout revoga a sessão JWT** no banco (`sessao.data_revogacao`); o token deixa de valer na hora | `AuthService.logout`, `WebAuthController` |
| **Segredo JWT sem valor padrão no código**; agora é obrigatório via `jwt.secret` ou `JWT_SECRET` | `JwtUtil`, `application.properties` |
| `/api/tutors` (lista de todos os tutores) restrito a **veterinário e clínica** | `SecurityConfig` |
| A notificação de consulta concluída **não inclui mais o diagnóstico**, apenas avisa que o prontuário está disponível | `ConsultaService.completeConsulta` |
| Senha mínima passou de 3 para **6 caracteres** (`@Valid` também no cadastro Web) | `RegisterRequest`, `WebAuthController` |
| Respostas JSON de 401/403 com `charset=UTF-8` (acentos corretos) | `SecurityConfig` |

---

## 🎨 5. Interface (UI/UX) e idiomas

### Design system (`static/style.css`)

- ✅ **216 estilos inline → 0.** Tudo virou classe: `.page`, `.card`, `.section-title`, `.grid-forms`, `.data-table`, `.record`, `.status-badge`, `.btn-cancel`...
- ✅ **Botões destrutivos** (Cancelar, Revogar, Desvincular, Sair) em vermelho, distintos da ação principal.
- ✅ **Badges de status por cor:** azul = agendado, verde = concluída/ativa, vermelho = cancelada/revogada/inativo.
- ✅ **Responsivo:** testado em 390px (iPhone), sem rolagem horizontal.
- ✅ **Acessibilidade:** `label for` em todos os campos, `role="alert"`, foco visível, `prefers-reduced-motion` e contraste melhorado no `--text-muted`.
- ✅ **Tema claro e escuro** funcionando, sem "piscar" ao carregar.

### Telas (`templates/`)

| Antes | Depois |
|---|---|
| `header.html` + `footer.html` e o resto repetido em cada página | `fragments/layout.html` com `head`, `header`, `alerts`, `status`, `prontuarios` e `footer` |
| Datas cruas (`2026-09-12T14:00`) | `12/09/2026 14:00` |
| Status cru (`AGENDADO`, `CONCLUIDA`) | Traduzidos ("Agendado", "Concluída" / "Scheduled", "Completed") |
| Formulários de consulta e autorização aparecendo mesmo sem pet cadastrado | Aviso "Cadastre um pet para liberar esta ação" |
| Era possível escolher data no passado no agendamento | Campo `min` com a data/hora atual (o servidor também valida) |
| Clínica podia "vincular" quem já estava na equipe | Lista só veterinários ainda não vinculados |
| Cadastro sem CPF/CNPJ e sem CRMV | Campos adicionados |
| `onclick` e `onsubmit` inline | `data-confirm` tratado pelo `app.js` |

### Idiomas (`i18n/`)

- `messages_pt_BR.properties` → `messages.properties` (português como padrão).
- `messages_en.properties` com **as mesmas chaves** (verificado automaticamente).
- `setFallbackToSystemLocale(false)`: um idioma sem tradução cai em português, não no idioma do servidor.
- Mensagens de sucesso das ações também traduzidas (chaves `msg.*`).

---

## ⚙️ 6. Configuração e execução

| Item | O que mudou |
|---|---|
| `.gitignore` | `application-local.properties` e `application-prod.properties` **saíram** da lista de ignorados |
| `DataInitializer` | Dados de demonstração completos: consulta agendada, **consulta concluída com prontuário**, **autorização LGPD ativa** e a **Dra. Camila** (sem vínculo), para demonstrar vínculo e transferência |
| `README.md` | Reescrito: problema → solução, fluxos, decisões técnicas, tabela de disciplinas, segurança, endpoints, execução e equipe |

### Contas de demonstração (perfil `local`)

| Perfil | E-mail | Senha |
|---|---|---|
| 🐶 Tutor | `ana.silva@email.com` | `senha123` |
| 🩺 Veterinário | `dr.carlos@veterinaria.com` | `senha123` |
| 🩺 Veterinária (sem vínculo) | `dra.camila@veterinaria.com` | `senha123` |
| 🏥 Clínica | `contato@matriz.com` | `senha123` |

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local   # http://localhost:8080
```

---

## 🧪 7. Testes

```text
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Classe | Testes | Novidades |
|---|:-:|---|
| `ConsultaServiceTest` | 8 | 🆕 Não concluir consulta cancelada · 🆕 notificação sem dados clínicos (LGPD) |
| `ConsultaControllerTest` | 7 | Erros agora verificados como exceções de domínio (o `GlobalExceptionHandler` converte em 400/403/404) |
| `ClinicaControllerTest` | 5 | 🆕 Conta que não é clínica recebe 403 · 🆕 transferência para médico sem vínculo é recusada |
| `AuthServiceTest` | 5 | — |
| `AutorizacaoControllerTest` | 3 | Ajustado para a nova estrutura |
| `PetServiceTest` | 3 | — |
| `WebSessionTest` 🆕 | 3 | Redirecionamento por perfil e gravação dos dados na sessão |
| `ClyvoVeterinaryApplicationTests` | 1 | — |

---

## 🔍 8. Como tudo foi verificado

1. ✅ `./mvnw clean test`: **51/51** passando.
2. ✅ Aplicação rodando localmente (`local`) e fluxos executados por HTTP:

| Fluxo testado | Resultado |
|---|---|
| POST sem token CSRF | Bloqueado ✅ |
| Login dos 3 perfis | Redireciona ao painel correto ✅ |
| Tutor agenda consulta no passado | "A data da consulta não pode ser no passado." ✅ |
| Tutor cadastra pet / agenda consulta | Mensagens de sucesso ✅ |
| Veterinário conclui atendimento | Prontuário gravado ✅ |
| Veterinário tenta abrir o painel do tutor | "Acesso negado" ✅ |
| Clínica vincula a Dra. Camila | Equipe passa de 1 para 2 ✅ |
| Clínica transfere o Thor para a Dra. Camila | "Atendimento e autorização transferidos com sucesso!" ✅ |
| Logout e reuso da sessão antiga | Sessão revogada, volta ao login ✅ |
| API: 404, 403, 401 e Swagger | JSON correto, Swagger respondendo ✅ |

3. ✅ Prints com Chrome headless: login, tutor, veterinário, clínica em inglês, celular 390px, tema claro e escuro. **Nenhum erro de JavaScript e nenhuma rolagem horizontal.**

---

## ✅ 9. O que falta fazer

> Marque `[x]` conforme for concluindo. A prioridade segue o peso da penalidade na rubrica.

### 🔴 Prioridade 1: bloqueadores (podem zerar ou tirar muitos pontos)

- [ ] **Revisar as alterações** (`git diff`) e rodar localmente para conferir.
- [ ] **Commitar dividindo entre os 4 integrantes** (*ausência de colaboração = −10*). Sugestão de divisão:

  | Integrante | Parte sugerida | Arquivos |
  |---|---|---|
  | Maicon | Segurança e autenticação | `config/*`, `AuthService`, `WebAuthController`, `dto/*` |
  | Gabriel | Regras de negócio e exceções | `services/*` (exceto Auth), `exceptions/*`, `models/*` |
  | Charlles | Interface e idiomas | `templates/**`, `static/**`, `i18n/**` |
  | Iago | Testes, README e configs | `src/test/**`, `README.md`, `EXECUCOES.md`, `.gitignore`, `application-*.properties` |

  Cada um cria uma branch, commita **pela própria conta do GitHub**, abre um **Pull Request** e outro integrante revisa e aprova antes do merge.
- [ ] **Fazer o deploy online** (*sem link = perde a maior parte dos 40 pontos de demonstração*):
  ```bash
  # no repositório DevOps-Clyvo-M_Vet, depois do push na main desta API
  export DB_USER="rmXXXXXX" DB_PASS="****" DEMO_PASSWORD="senha123"
  export JWT_SECRET="$(openssl rand -base64 48)"
  ./scripts/deploy_acr_aci.sh
  ```
- [ ] Abrir a URL gerada e **testar os 3 perfis online** antes de gravar.
- [ ] Colocar a **URL da aplicação e do Swagger** no topo do `README.md`.

### 🟠 Prioridade 2: repositório DevOps (consistência)

- [x] `docker-compose.yml` agora sobe **Oracle Free** (`gvenzl/oracle-free`), sem senha padrão no arquivo.
- [x] `scripts/deploy_app_service.sh` (PostgreSQL + senhas fixas) **removido**: a entrega usa só ACR + ACI.
- [x] README do DevOps: arquitetura, deploy manual e compose alinhados ao Oracle da FIAP.
- [ ] **Commitar e dar push no repositório DevOps** também (as mudanças estão só no disco).

### 🟡 Prioridade 3: README (narrativa e multidisciplinar, 40 pontos)

Procure por **⚠️ PREENCHER** no `README.md`:

- [ ] Nomes **oficiais** das disciplinas do semestre e como cada uma foi aplicada.
- [x] **DER** em [`docs/DER.md`](docs/DER.md) (Mermaid, gerado das migrations).
- [ ] Evidências: **Business Model Canvas**, **protótipo (Figma)** e link do **app mobile** em [`docs/`](docs/README.md).
- [ ] (Opcional) Dados que validam o problema (pesquisa, entrevistas).
- [ ] GitHub e **responsabilidades de cada integrante**.
- [ ] Link do **vídeo final**.
- [ ] Conferir se o `Documentacao_Arquitetura_ClyvoVet.pdf` está atualizado (ele foi feito antes destas mudanças).

### 🟢 Prioridade 4: vídeo (até 15 minutos, todos falando)

- [ ] Gravar **usando a aplicação online**, não o localhost.
- [ ] Roteiro sugerido:

  | Tempo | Parte | Quem |
  |---|---|---|
  | 0–1 min | O problema | ⚠️ |
  | 1–3 min | A solução e as decisões técnicas | ⚠️ |
  | 3–9 min | Demonstração: tutor → veterinário → tutor → clínica | ⚠️ |
  | 9–12 min | Disciplinas e evidências (Canvas, DER, DevOps, mobile) | ⚠️ |
  | 12–14 min | Código: services, segurança, testes rodando | ⚠️ |
  | 14–15 min | Encerramento | ⚠️ |

- [ ] Na demonstração, mostrar também: troca **PT/EN**, **tema claro/escuro**, **celular** e **Swagger**.
- [ ] Ensaiar antes: erro visual ou de fluxo durante a apresentação = **−5**.

### ⚪ Opcional (se sobrar tempo)

- [x] Criar DTOs de entrada na API REST, mantendo o mesmo formato JSON do mobile.
- [x] Tela de **notificações** na interface Web.
- [x] Testes de integração com `MockMvc` cobrindo CSRF e os redirecionamentos Web.

### 📦 Entrega

- [ ] Repositório GitHub com README completo.
- [ ] Link da aplicação online.
- [ ] Vídeo (≤ 15 minutos).
- [ ] **Enviar pelo portal dentro do prazo** (*fora do prazo ou fora do portal = −100*).

---

## 🆕 Segunda rodada (22/10): DER, DTOs, notificações e testes de integração

| Entrega | Arquivos |
|---|---|
| 🗄️ **DER** em Mermaid (o GitHub renderiza), com as decisões de modelagem e o índice de evidências | `docs/DER.md`, `docs/README.md` |
| 📥 **DTOs de entrada** `PetRequest` e `ConsultaRequest` (records com Bean Validation). Mesmo JSON do mobile, mas id, tutor e status não podem ser enviados pelo cliente | `dto/`, `PetController`, `ConsultaController` |
| 🧹 DTO `ScheduleAppointmentRequest` removido (não era usado em lugar nenhum) | `dto/` |
| 🔔 **Caixa de notificações na Web** para os 3 perfis: sino com contador de não lidas no cabeçalho, lista com destaque das novas e "marcar todas como lidas" (PT/EN, responsiva) | `WebNotificacaoController`, `WebHeaderAdvice`, `web/notificacoes.html`, `layout.html`, `style.css`, `i18n/` |
| 🔔 API: `GET /api/notificacoes/nao-lidas` e `PUT /api/notificacoes/lidas` | `NotificacaoController`, `NotificacaoService` |
| 🎬 Dados de demonstração com notificações (a caixa não aparece vazia no vídeo) | `DataInitializer` |
| 🧪 **15 testes de integração** (MockMvc + contexto Spring completo) | `integration/SegurancaEFluxosIntegrationTest` |
| 🧪 Os testes agora usam **as migrations Flyway** em vez do schema gerado pelo Hibernate, então rodam sobre os mesmos tipos de coluna do Oracle | `application-test.properties` |

### 🐞 Bugs encontrados pelos novos testes

| # | Problema | Impacto | Correção |
|:-:|---|---|---|
| 9 | Dois logins da mesma conta no mesmo segundo geravam **o mesmo JWT**, e a busca da sessão pelo hash quebrava (`NonUniqueResultException`) | ❌ Erro 500 ao logar no app e na Web ao mesmo tempo (ou com duplo clique) | Cada token ganhou um id único (`jti`) em `JwtUtil` |
| 10 | Cadastro com **CPF, CNPJ ou CRMV já usado** estourava a constraint `uk_tipo_valor` | ❌ Erro 500 na tela de cadastro | `AuthService` valida antes e responde "CPF já cadastrado no sistema." (400) |
| 11 | `SecurityUtils` tratava o usuário anônimo como se fosse um id de conta | ⚠️ Exceção em páginas públicas | Usuário anônimo é ignorado (`findLoggedAccountId()`) |

> ℹ️ **Ponto de atenção (LGPD):** a coluna `identificador_acesso.valor_hash` guarda o CPF/CNPJ **em texto puro**, apesar do nome. Se o professor perguntar, a melhoria seria gravar o hash (SHA-256) e usar `valor_criptografado` para exibição.

---

## 🗂️ 10. Mapa de arquivos alterados

<details>
<summary><strong>🆕 Arquivos novos (clique para expandir)</strong></summary>

| Arquivo | Função |
|---|---|
| `config/SecurityUtils.java` | Retorna a conta autenticada (REST e Web) |
| `config/WebSession.java` | Atributos da sessão Web e redirecionamento por perfil |
| `controllers/web/WebFeedback.java` | Padrão PRG com mensagens flash |
| `exceptions/DomainException.java` | Base das exceções de negócio |
| `templates/fragments/layout.html` | Componentes reutilizáveis das telas |
| `test/.../config/WebSessionTest.java` | Testes do redirecionamento e da sessão |
| `application-local.properties` · `application-prod.properties` | Agora versionados |
| `EXECUCOES.md` | Este documento |

</details>

<details>
<summary><strong>✏️ Arquivos modificados</strong></summary>

| Camada | Arquivos |
|---|---|
| Config | `SecurityConfig`, `JwtUtil`, `JwtFilter`, `CustomOAuth2SuccessHandler`, `DataInitializer`, `InternacionalizacaoConfig` |
| Controllers REST | `ConsultaController`, `PetController`, `AppointmentController`, `NotificacaoController`, `AutorizacaoController`, `ClinicaController` |
| Controllers Web | `WebAuthController`, `WebTutorController`, `WebVetController`, `WebClinicaController` |
| Services | `AuthService`, `ConsultaService`, `AutorizacaoService`, `ClinicaService`, `PetService`, `NotificacaoService` |
| Models e enums | `Consulta`, `AutorizacaoAcessoPet`, `VeterinarioClinica`, `TipoConta`, `StatusConsulta`, `StatusAutorizacao`, `StatusConta`, `StatusVinculo` |
| DTOs e exceções | `RegisterRequest`, `AuthRequest`, `AccessDeniedException`, `AccountBlockedException`, `BusinessException`, `ResourceNotFoundException` |
| Front-end | `style.css`, `app.js`, `login.html`, `dashboard-tutor.html`, `dashboard-vet.html`, `dashboard-clinica.html` |
| i18n | `messages.properties` (antigo `messages_pt_BR`), `messages_en.properties` |
| Testes | `ConsultaServiceTest`, `ConsultaControllerTest`, `ClinicaControllerTest`, `AutorizacaoControllerTest` |
| Outros | `README.md`, `.gitignore`, `application.properties` |

</details>

<details>
<summary><strong>🗑️ Arquivos removidos</strong></summary>

| Arquivo | Motivo |
|---|---|
| `static/css/main.css` | Nenhuma página usava |
| `templates/fragments/header.html` · `footer.html` | Substituídos por `fragments/layout.html` |

</details>

---

<p align="center">🐾 <strong>Clyvo Vet</strong>: revisão para a entrega final</p>

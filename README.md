# 🐾 Clyvo Vet — Plataforma Integrada de Saúde Veterinária

> Tutores, médicos veterinários e clínicas na mesma plataforma, com **prontuário digital compartilhado** e **consentimento de acesso controlado pelo tutor (LGPD)**.

![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR%20%2B%20i18n-005F0E?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle_Database-FIAP-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-V1..V7-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![IA](https://img.shields.io/badge/IA-Claude%20(Anthropic)-D97757?style=for-the-badge)
![Tests](https://img.shields.io/badge/Testes-64%20passando-brightgreen?style=for-the-badge)

| | |
|---|---|
| 🌐 **Aplicação online** | [http://clyvovet-app-31961.brazilsouth.azurecontainer.io:8080/](http://clyvovet-app-31961.brazilsouth.azurecontainer.io:8080/) (Azure Container Instances, região Brazil South) |
| 📘 **Swagger (API REST)** | [http://clyvovet-app-31961.brazilsouth.azurecontainer.io:8080/swagger-ui.html](http://clyvovet-app-31961.brazilsouth.azurecontainer.io:8080/swagger-ui.html) |
| 🎥 **Vídeo da apresentação** | ⚠️ **PREENCHER com o vídeo da entrega final** |
| ☁️ **Repositório DevOps** | [MCGI-PW/DevOps-Clyvo-M_Vet](https://github.com/MCGI-PW/DevOps-Clyvo-M_Vet) |

---

## 📌 Sumário

1. [O Problema](#-o-problema)
2. [A Solução](#-a-solução)
3. [Principais Fluxos](#-principais-fluxos)
4. [Inteligência Artificial: Assistente da Teleconsulta](#-inteligência-artificial-assistente-da-teleconsulta)
5. [Decisões de Design e Tecnologia](#-decisões-de-design-e-tecnologia)
6. [Integração Multidisciplinar](#-integração-multidisciplinar)
7. [Arquitetura e Estrutura do Projeto](#-arquitetura-e-estrutura-do-projeto)
8. [Banco de Dados (Oracle + Flyway)](#-banco-de-dados-oracle--flyway)
9. [Segurança](#-segurança)
10. [Endpoints da API REST](#-endpoints-da-api-rest)
11. [Como Executar](#-como-executar)
12. [Testes Automatizados](#-testes-automatizados)
13. [Equipe e Colaboração](#-equipe-e-colaboração)

---

## ❓ O Problema

O histórico de saúde de um animal fica **espalhado** entre as clínicas e os veterinários que já o atenderam. Na prática:

* O tutor que troca de clínica ou procura um especialista **não leva o histórico junto**: exames, diagnósticos e prescrições ficam presos em papel ou no sistema de cada clínica.
* O veterinário atende **sem contexto clínico** do paciente, o que gera retrabalho e mais risco.
* O tutor **não sabe nem controla** quem acessa os dados do seu animal, e esses dados também envolvem dados pessoais dele (LGPD).
* Clínicas com vários veterinários não têm um jeito simples de **transferir o acompanhamento** de um paciente entre profissionais.

> ⚠️ **PREENCHER (opcional):** dados ou entrevistas que o grupo usou para validar o problema (pesquisa com tutores, conversa com clínicas etc.).

## 💡 A Solução

O **Clyvo Vet** conecta os três atores em um só lugar, com o **tutor no controle dos dados do pet**:

| Perfil | O que faz na plataforma |
|---|---|
| 🐶 **Tutor** | Cadastra os pets, agenda consultas (presenciais ou por telemedicina), consulta o **prontuário** de cada pet, **concede ou revoga** o acesso de veterinários e clínicas e recebe **notificações** a cada etapa. |
| 🩺 **Veterinário** | Vê a fila de atendimentos, conclui consultas registrando **diagnóstico, prescrição e orientações** no prontuário e consulta os prontuários que já emitiu. |
| 🏥 **Clínica** | Gerencia o corpo clínico (vincula e desvincula profissionais), acompanha os agendamentos da unidade e **transfere o atendimento** de um paciente entre veterinários. |

**Diferenciais:**

* **Consentimento como regra de negócio:** ao agendar, o sistema cria automaticamente a autorização de acesso ao prontuário. Se o tutor revogar, as consultas pendentes com aquele profissional são **canceladas em cascata**.
* **Prontuário separado da consulta (3FN):** a anotação clínica é um registro próprio, com médico responsável e data. A notificação ao tutor **não expõe dados clínicos**, apenas avisa que o prontuário está disponível.
* **Multi-tenancy por clínica:** cada clínica enxerga só as próprias consultas, a própria equipe e as próprias autorizações.
* **Caixa de notificações (🔔):** os três perfis são avisados de agendamentos, cancelamentos, prontuários e vínculos, com contador de não lidas no cabeçalho.
* **Uma regra de negócio, dois canais:** a mesma camada de serviço atende a interface Web (Thymeleaf) e a API REST consumida pelo aplicativo mobile.

## 🔁 Principais Fluxos

```text
TUTOR                         VETERINÁRIO                        CLÍNICA
  │ cadastra pet                  │                                 │ vincula veterinário à equipe
  │ agenda consulta ─────────────►│ recebe na fila                  │
  │   └─ autorização LGPD criada  │                                 │
  │                               │ conclui atendimento             │
  │◄──── notificação ─────────────│   └─ grava prontuário           │
  │ lê o prontuário do pet        │                                 │
  │                               │◄──────── transfere paciente ────│ (só para médico com vínculo ativo)
  │ revoga consentimento ────────►│ consultas pendentes canceladas  │
```

**Roteiro sugerido para a demonstração** (dados de demonstração já carregados):

1. Login como **Tutor** → abrir o 🔔 (notificações) → cadastrar pet → agendar consulta → ver prontuário existente do Thor.
2. Login como **Veterinário** → concluir a consulta preenchendo diagnóstico e prescrição.
3. Login como **Tutor** → ver o novo prontuário → revogar um consentimento.
4. Login como **Clínica** → vincular a Dra. Camila → transferir o paciente Thor para ela.
5. Trocar idioma (PT/EN) e tema (claro/escuro); mostrar o Swagger.

## 🤖 Inteligência Artificial: Assistente da Teleconsulta

**Problema:** durante a teleconsulta o veterinário divide a atenção entre o paciente, o tutor e o teclado. O prontuário costuma ser escrito depois, de memória, e o tutor sai da chamada sem um resumo do que precisa fazer em casa.

**Solução:** um assistente que **ouve a consulta e escreve por você**, no espírito da tradução simultânea dos fones de ouvido: o texto aparece em tempo real enquanto se fala e, ao final, a IA organiza tudo em prontuário.

```text
 🎙️ Microfone do navegador          ✨ IA (servidor)                          🩺 Veterinário
 Web Speech API, pt-BR    ──texto──► Claude (LLM) + saída estruturada ─────► revisa, ajusta e
 legenda ao vivo na tela             (JSON Schema do record ResumoClinico)     finaliza o atendimento
 ou transcrição colada               └─ fallback: assistente local por regras        │
 de uma consulta gravada             grava em TRANSCRICAO_CONSULTA (V7)              ▼
                                                                      PRONTUARIO + notificação ao tutor
```

| Etapa | Como funciona | Onde está |
|---|---|---|
| **1. Transcrição em tempo real** | O navegador reconhece a fala (Web Speech API, `pt-BR`/`en-US` conforme o idioma da tela). Frases parciais aparecem em itálico como legenda e as finais entram no campo de transcrição. Também aceita colar a transcrição de uma **consulta gravada**. | [`app.js`](src/main/resources/static/app.js) · [`dashboard-vet.html`](src/main/resources/templates/web/dashboard-vet.html) |
| **2. Resumo com LLM** | O servidor envia a transcrição ao **Claude** (SDK oficial `anthropic-java`) com um prompt de documentação clínica e **saída estruturada**: o schema JSON vem do record [`ResumoClinico`](src/main/java/com/clyvo/veterinary/services/ia/ResumoClinico.java), então a resposta já chega validada com `diagnostico`, `prescricao`, `orientacoes`, `resumoTutor` e `sinaisDeAlerta`. | [`ClaudeAssistenteClinico`](src/main/java/com/clyvo/veterinary/services/ia/ClaudeAssistenteClinico.java) |
| **2b. Serviço de IA ClyvoScribe** | Com `CLYVOSCRIBE_URL` configurada, o resumo é feito pelo **[ClyvoScribe AI](https://github.com/MCGI-PW/IoT-Clyvo-Vet_M)**, o serviço de IA da CLYVO VET em Python (`POST /api/transcriptions/summarize`). Ele usa o Claude ou o seu extrator local e acrescenta alertas de segurança clínica (ex.: raças sensíveis a ivermectina). | [`ClyvoScribeAssistenteClinico`](src/main/java/com/clyvo/veterinary/services/ia/ClyvoScribeAssistenteClinico.java) |
| **3. Fallback local** | Sem chave de API, ou se a IA externa falhar ou recusar, um classificador por palavras-chave separa as frases nas mesmas categorias. A consulta nunca trava por causa da IA. | [`AssistenteClinicoLocal`](src/main/java/com/clyvo/veterinary/services/ia/AssistenteClinicoLocal.java) |
| **4. Humano no controle** | As sugestões **pré-preenchem** os campos do prontuário (destacados). Nada entra no prontuário sem o veterinário revisar e clicar em finalizar. | [`TranscricaoService`](src/main/java/com/clyvo/veterinary/services/TranscricaoService.java) |
| **5. Persistência e auditoria** | Transcrição, sugestões e a fonte (`CLYVOSCRIBE`, `CLAUDE:<modelo>` ou `LOCAL`) ficam em `TRANSCRICAO_CONSULTA`, ligadas à consulta. | [`V7__Create_Transcricao_Consulta.sql`](src/main/resources/db/migration/V7__Create_Transcricao_Consulta.sql) |

**Cuidados (LGPD e segurança clínica):**
* Só o veterinário responsável (ou a clínica da consulta) pode gerar o resumo; consulta cancelada é rejeitada.
* Para a IA vão só o nome e a raça do pet e a transcrição. Nome, CPF e contato do tutor não são enviados.
* O prompt proíbe inventar diagnóstico, medicamento ou dose que não esteja na fala; campo sem informação fica vazio.

**Escolha da IA** (ordem de prioridade; sem nenhuma variável, o assistente local é usado automaticamente):

```bash
# 1. Serviço ClyvoScribe AI (repositório IoT-Clyvo-Vet_M rodando com: python3 run_server.py)
export CLYVOSCRIBE_URL="http://localhost:8000"

# 2. Ou Claude direto, sem o serviço Python
export ANTHROPIC_API_KEY="sua_chave"
export CLYVO_IA_MODEL="claude-opus-5-5"   # opcional, este já é o padrão
```

Se o ClyvoScribe ou o Claude estiverem fora do ar, a API cai no assistente local e a consulta continua.

**Demonstração:** login como **Veterinário** → na consulta do Thor, clique em **🎙️ Iniciar transcrição** e fale a consulta (ou cole o roteiro abaixo) → **✨ Gerar resumo com IA** → revise os campos preenchidos → **Finalizar atendimento** → login como **Tutor** para ver o prontuário.

> Roteiro de exemplo: *"O Thor está coçando muito a orelha desde sábado. Pelo exame parece ser uma otite externa bacteriana. Vou prescrever uma solução otológica, 3 gotas duas vezes ao dia por 7 dias. Mantenha a orelha seca e evite banho nesse período. Se ele tiver febre ou parar de comer, procure atendimento imediatamente. Retorno em 10 dias para reavaliação."*

A transcrição ao vivo usa o reconhecimento de voz do navegador (Chrome e Edge; no Safari depende da versão). Em qualquer navegador dá para colar o texto.

## 🧭 Decisões de Design e Tecnologia

| Decisão | Justificativa |
|---|---|
| **Spring Boot 3 + Java 21 LTS** | Stack trabalhada na disciplina, com suporte de longo prazo e ecossistema maduro (Security, Data JPA, Validation). |
| **Thymeleaf (SSR) + API REST** | As telas Web são renderizadas no servidor (sessão + CSRF), e a API JSON stateless (JWT) atende o app mobile. As duas camadas usam os **mesmos services**, sem duplicar regra de negócio. |
| **Oracle + Flyway** | O banco da FIAP é Oracle. As migrations versionadas (`V1..V7`) garantem que qualquer ambiente sobe com o mesmo schema. Localmente usamos H2 em `MODE=Oracle`. |
| **Modelo em 3FN** | Conta de acesso, credencial, identificadores e perfis separados. O prontuário é separado da consulta, e as autorizações de acesso são uma entidade própria. |
| **JWT com sessão revogável** | O token é assinado (HMAC) e tem o hash registrado na tabela `sessao`; o logout revoga a sessão antes da expiração. |
| **Padrão Post-Redirect-Get + flash messages** | Formulários não são reenviados com F5 e as mensagens de retorno não trafegam pela URL. |
| **Design system próprio (Aurora)** | Tokens de cor e tema claro/escuro com contraste acessível, layout responsivo (testado em 390px), ações destrutivas em vermelho com confirmação e i18n completo (PT-BR/EN). |

## 🎓 Integração Multidisciplinar

> ⚠️ **PREENCHER pelo grupo:** confirme os nomes oficiais das disciplinas do semestre e anexe as evidências (links ou arquivos na pasta `docs/`).

| Disciplina | Como foi aplicada na solução | Evidência |
|---|---|---|
| **Java Advanced / Spring** | API REST, Thymeleaf, Spring Security (RBAC + JWT + OAuth2), camada de serviços, tratamento global de exceções, i18n e testes JUnit/Mockito. | Este repositório |
| **Banco de Dados (Oracle)** | Modelagem em 3FN, scripts DDL versionados com Flyway e constraints de integridade. | [`src/main/resources/db/migration`](src/main/resources/db/migration) · [DER](docs/DER.md) |
| **DevOps & Cloud** | Dockerfile multi-stage com usuário não-root, build no Azure Container Registry e deploy no Azure Container Instances via Azure CLI. | [DevOps-Clyvo-M_Vet](https://github.com/MCGI-PW/DevOps-Clyvo-M_Vet) |
| **Mobile** | ⚠️ App que consome esta API (`/api/**`, JWT Bearer). | ⚠️ link do repositório mobile |
| **QA / Testes** | 64 testes automatizados (unitários + integração com MockMvc sobre o schema real do Flyway), cobrindo regras de negócio e segurança. | [`src/test`](src/test) |
| **⚠️ Negócios / Canvas / UX** | ⚠️ Business Model Canvas, personas e protótipo (Figma). | ⚠️ links |

📄 Documento de arquitetura: [`Documentacao_Arquitetura_ClyvoVet.pdf`](Documentacao_Arquitetura_ClyvoVet.pdf) · 📁 Evidências: [`docs/`](docs/README.md) · 🗄️ [DER](docs/DER.md)

## 🏛 Arquitetura e Estrutura do Projeto

```text
com.clyvo.veterinary/
├── config/          Segurança (SecurityConfig, JwtFilter, JwtUtil, SecurityUtils, WebSession),
│                    OAuth2, i18n, Swagger e carga de dados de demonstração (DataInitializer)
├── controllers/     API REST (JSON) — consumida pelo app mobile e documentada no Swagger
│   └── web/         Controllers MVC das telas Thymeleaf (padrão PRG via WebFeedback)
├── dto/             Payloads de entrada/saída com Bean Validation
├── exceptions/      DomainException → Business(400) / AccessDenied(403) / NotFound(404) / AccountBlocked(403)
│                    + GlobalExceptionHandler (respostas JSON padronizadas)
├── models/          Entidades JPA (3FN) e enums de domínio (status, perfis, modalidade)
├── repositories/    Spring Data JPA
└── services/        Regras de negócio (única fonte de verdade para Web e REST)
    └── ia/          Assistente de IA da consulta: ClyvoScribe (serviço Python), Claude ou fallback local

resources/
├── db/migration/    Flyway V1..V7 (Oracle)
├── i18n/            messages.properties (PT-BR) e messages_en.properties
├── templates/       fragments/layout.html (cabeçalho, alertas, status, prontuários, rodapé) + páginas
└── static/          style.css (design system) e app.js (tema claro/escuro e confirmações)
```

## 🗄 Banco de Dados (Oracle + Flyway)

| Migration | Conteúdo |
|---|---|
| `V1__Create_3NF_Core.sql` | `conta_acesso`, `credencial`, `identificador_acesso`, `tutor`, `veterinario`, `clinica`, `veterinario_clinica`, `autorizacao_acesso_pet`, `sessao` |
| `V2__Create_Racas.sql` | Catálogo normalizado de espécies e raças |
| `V3__Create_Consulta_And_Populate_Vets.sql` | Tabela `consulta` |
| `V4__Create_Notificacao.sql` | Caixa de entrada de notificações |
| `V5__Add_Clinica_To_Consulta_And_Autorizacao.sql` | Multi-tenancy: `id_clinica` em consultas e autorizações |
| `V6__Create_Prontuario.sql` | Prontuário clínico (FK para consulta e veterinário) |
| `V7__Create_Transcricao_Consulta.sql` | Transcrição da teleconsulta (`CLOB`) e sugestões geradas pela IA, com a fonte do resumo |

Padrões Oracle: chaves `RAW(16) DEFAULT SYS_GUID()`, booleanos `NUMBER(1) CHECK (IN (0,1))` e textos longos `VARCHAR2(4000)`.

## 🔒 Segurança

* **RBAC:** perfis `TUTOR`, `VETERINARIO` e `CLINICA` protegem as rotas Web e REST, e os services validam a titularidade de cada recurso (ex.: tutor só agenda para o próprio pet; clínica só transfere para médico com vínculo ativo).
* **Anti força bruta:** 5 senhas erradas seguidas bloqueiam a conta por 15 minutos; a mensagem é a mesma para usuário inexistente e senha errada (anti-enumeração).
* **CSRF** ativo nas telas Web; a API REST é stateless (Bearer) e fica fora dessa verificação.
* **Sem segredos no código:** `JWT_SECRET`, `DB_URL`, `DB_USER`, `DB_PASS` e `DEMO_PASSWORD` vêm de variáveis de ambiente. Contas de demonstração só existem nos perfis `local`, `dev` e `demo`.
* **LGPD:** acesso ao prontuário condicionado ao consentimento do tutor, revogação a qualquer momento e notificações sem dados clínicos.

## 📡 Endpoints da API REST

Documentação interativa: **`/swagger-ui.html`** (use `POST /api/auth/login` e depois **Authorize** com o token).

| Recurso | Método e rota | Perfil |
|---|---|---|
| Autenticação | `POST /api/auth/register` · `POST /api/auth/login` | público |
| Pets | `GET /api/pets` · `POST /api/pets` (DTO `PetRequest`) | tutor |
| Consultas | `GET /api/consultas` · `POST /api/consultas` (DTO `ConsultaRequest`) · `PUT /api/consultas/{id}/cancelar` | autenticado |
| Atendimento | `POST /api/appointments/{id}/complete` | veterinário / clínica |
| IA da consulta | `POST /api/appointments/{id}/ai-summary` `{"transcript": "..."}` · `GET /api/appointments/{id}/ai-summary` | veterinário / clínica |
| Clínicas | `GET /api/clinicas` · `GET /api/clinicas/{id}/veterinarios` · `GET /api/clinicas/minha` | autenticado / clínica |
| Corpo clínico | `POST /api/clinicas/veterinarios/vincular` · `PUT /api/clinicas/veterinarios/{id}/desvincular` | clínica |
| Transferência | `PUT /api/clinicas/autorizacoes/{id}/transferir` | clínica |
| Autorizações | `GET /api/autorizacoes` · `PUT /api/autorizacoes/{id}/revogar` | tutor |
| Notificações | `GET /api/notificacoes` · `GET /api/notificacoes/nao-lidas` · `PUT /api/notificacoes/lidas` | autenticado |

Erros sempre no formato `{"error": "mensagem"}` com o status HTTP correto (400, 401, 403 ou 404). Erros de validação dos DTOs (Bean Validation) voltam como `{"campo": "mensagem"}`.

**DTOs de entrada:** `PetRequest` e `ConsultaRequest` mantêm o mesmo formato JSON usado pelo app mobile (`{"pet": {"idPet": "..."}}`), mas só aceitam os campos que o cliente pode informar: id, tutor e status são sempre definidos pelo servidor (proteção contra *mass assignment*).

## 🚀 Como Executar

**Pré-requisito:** Java 21.

### 1. Local (H2 em memória, sem configuração)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Acesse **http://localhost:8080**. As migrations rodam sozinhas e os dados de demonstração são carregados.

| Perfil | E-mail | Senha |
|---|---|---|
| Tutor | `ana.silva@email.com` | `senha123` |
| Veterinário | `dr.carlos@veterinaria.com` | `senha123` |
| Veterinária (sem vínculo, para demonstrar vínculo e transferência) | `dra.camila@veterinaria.com` | `senha123` |
| Clínica | `contato@matriz.com` | `senha123` |

Troca de idioma: `?lang=pt_BR` ou `?lang=en` (ou o seletor PT/EN no cabeçalho).

### 2. Oracle da FIAP

```bash
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USER="seu_rm"
export DB_PASS="sua_senha"
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Para incluir os dados de demonstração no Oracle, use `prod,demo` e defina `DEMO_PASSWORD`.

### 3. Nuvem (Azure)

O deploy (ACR + ACI na região `brazilsouth`, perfil `prod,demo`) está no [repositório DevOps](https://github.com/MCGI-PW/DevOps-Clyvo-M_Vet): `scripts/deploy_acr_aci.sh`.

## 🧪 Testes Automatizados

```bash
./mvnw clean test
```

**64 testes** (JUnit 5 + Mockito + MockMvc, padrão Arrange-Act-Assert):

* `ConsultaServiceTest`: pet de outro tutor (403), data no passado, conflito de agenda, agendamento válido com autorização LGPD, cancelamento, conclusão com prontuário, bloqueio de conclusão de consulta cancelada e notificação sem dados clínicos.
* `AuthServiceTest`: cadastro, perfil inválido, login, bloqueio após 5 tentativas e conta bloqueada.
* `ConsultaControllerTest`, `ClinicaControllerTest`, `AutorizacaoControllerTest`: multi-tenancy, vínculo, transferência (inclusive para médico sem vínculo), revogação e erros 403, 404 e 400.
* `TranscricaoServiceTest` e `AssistenteClinicoLocalTest` (IA): resumo estruturado gravado com a fonte, fallback local quando a IA externa falha ou não há chave, escolha da IA pela configuração, bloqueio de veterinário de outra consulta, consulta cancelada e extração sem inventar conteúdo.
* `ClyvoScribeAssistenteClinicoTest`: integração HTTP com o serviço de IA em Python (simulado por um servidor local no teste), incluindo erro HTTP e serviço fora do ar.
* `PetServiceTest`, `WebSessionTest` e `ClyvoVeterinaryApplicationTests` (contexto Spring).
* `SegurancaEFluxosIntegrationTest` (integração, contexto completo + H2 com as migrations Flyway): 401/403 na API, redirecionamentos Web, CSRF, RBAC por perfil, logout revogando o JWT, login por perfil, cadastro de pet via Web (PRG), validação dos DTOs, CPF duplicado e caixa de notificações, além de RBAC e validação do endpoint de resumo por IA.

## 👥 Equipe e Colaboração

| Integrante | RM | GitHub | Principais responsabilidades |
|---|---|---|---|
| Maicon Douglas | RM561279 | [@MaiconDouglas-dev](https://github.com/MaiconDouglas-dev) | ⚠️ PREENCHER |
| Gabriel (Gabs) | RM561551 | ⚠️ @usuario | ⚠️ PREENCHER |
| Charlles | RM566482 | ⚠️ @usuario | ⚠️ PREENCHER |
| Iago D. | RM565708 | ⚠️ @usuario | ⚠️ PREENCHER |

Fluxo de trabalho: branches por funcionalidade → Pull Request → revisão por outro integrante → merge na `main`.

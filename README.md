# 🐾 Clyvo Veterinary Platform (`api-clyvovet_M`)

> Plataforma Integrada de Gestão Veterinária e API RESTful desenvolvida com **Spring Boot 3**, **Java 21 LTS**, **Thymeleaf (SSR)**, persistência compatível com **Oracle Database (FIAP)** / **H2**, versionamento via **Flyway** e modelagem normalizada na **3ª Forma Normal (3NF)**.

![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR%20%2B%20i18n-005F0E?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle_Database-FIAP%20Ready-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-V1..V6%20ANSI%20SQL-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-RBAC%20Enabled-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

---

## 📌 Sumário

* [Visão Geral](#-visão-geral)
* [Diferenciais de Engenharia e Arquitetura](#-diferenciais-de-engenharia-e-arquitetura)
* [Tecnologias Utilizadas](#-tecnologias-utilizadas)
* [Estrutura do Projeto](#-estrutura-do-projeto)
* [Banco de Dados e Migrations Flyway (Oracle ANSI SQL)](#-banco-de-dados-e-migrations-flyway-oracle-ansi-sql)
* [Segurança, RBAC e Anti-Brute Force](#-segurança-rbac-e-anti-brute-force)
* [Interface Web Thymeleaf e Internacionalização (i18n)](#-interface-web-thymeleaf-e-internacionalização-i18n)
* [Endpoints da API REST (Compatibilidade Mobile)](#-endpoints-da-api-rest-compatibilidade-mobile)
* [Como Executar o Projeto](#-como-executar-o-projeto)
* [Testes Automatizados (Padrão AAA)](#-testes-automatizados-padrão-aaa)
* [Autor](#-autor)

---

## 📖 Visão Geral

O **Clyvo Vet** é uma solução completa desenvolvida para o ecossistema de cuidados animais, integrando três perfis principais em uma única plataforma robusta:

1. **Tutores**: Cadastro de animais de estimação, agendamento de consultas médicas presenciais ou online, acompanhamento de notificações e gestão de consentimento de acesso clínico (em conformidade com a LGPD).
2. **Médicos Veterinários**: Gestão da fila de consultas agendadas, atendimento médico e preenchimento de **Prontuário Clínico** com persistência relacional.
3. **Clínicas Veterinárias**: Credenciamento e vínculo de médicos veterinários, isolamento multi-tenancy de atendimentos e transferência interna de consultas.

---

## 🚀 Diferenciais de Engenharia e Arquitetura

* **Camada de Apresentação Híbrida (Two-Tier Presentation)**:
  * **Web Server-Side Rendering (Thymeleaf + Spring MVC)**: Para navegadores desktop/mobile com sessões seguras (`JSESSIONID`), padrão Post-Redirect-Get (PRG) e suporte dinâmico a múltiplos idiomas (**i18n** PT-BR / EN).
  * **REST API Stateless**: Endpoints JSON documentados no Swagger UI, preservando **100% de retrocompatibilidade** com o aplicativo mobile já em produção.
* **Persistência Corporativa com Flyway no Oracle**: Substituição total de dialetos PostgreSQL por SQL ANSI nativo compatível com o **Oracle Database da FIAP** e H2 (`RAW(16)`, `DEFAULT SYS_GUID()`, `NUMBER(1)`, `VARCHAR2(4000)`).
* **Prontuário Clínico Normalizado (3NF)**: Criação da entidade `Prontuario` separada de `Consulta`, garantindo a 3ª Forma Normal e persistência definitiva das anotações médicas enviadas pelo aplicativo.
* **Segurança Baseada em Perfis (RBAC)**: Injeção de permissões reais no Spring Security (`ROLE_TUTOR`, `ROLE_VETERINARIO`, `ROLE_CLINICA`) via claims do JWT.
* **Proteção contra Força Bruta (Brute-Force Protection)**: Bloqueio automático de contas por 15 minutos após 5 tentativas consecutivas de senha incorreta e resposta unificada anti-enumeração.
* **Regras de Domínio Críticas**: Validação estrita de titularidade do pet (impede agendamento para pet alheio com HTTP 403), bloqueio de datas retroativas, validação de vínculo médico-clínica e prevenção de conflitos de horário na agenda médica.
* **Tratamento Global de Exceções**: Centralização via `@RestControllerAdvice` com respostas JSON no formato `{"error": "mensagem"}` sem quebra de contrato.

---

## 🛠 Tecnologias Utilizadas

| Tecnologia | Versão | Papel no Sistema |
|---|---|---|
| **Java** | 21 LTS | Linguagem principal de desenvolvimento |
| **Spring Boot** | 3.5.4 | Framework corporativo (MVC, Security, Data JPA, Validation) |
| **Thymeleaf** | 3.x | Engine de Server-Side Rendering para a interface Web |
| **Spring Security** | 6.x | Controle de autenticação híbrida (Web Session + JWT Stateless) |
| **JJWT** | 0.12.6 | Geração, validação e extração de claims RBAC dos tokens |
| **Oracle Database / H2** | H2 2.3+ / Oracle 19c+ | Banco de dados relacional com compatibilidade Oracle ANSI |
| **Flyway** | 10.x | Controle de versão de banco de dados (`flyway_schema_history`) |
| **SpringDoc OpenAPI** | 2.8.5 | Documentação interativa Swagger UI em `/swagger-ui.html` |
| **Maven Wrapper (`mvnw`)** | 3.9.16 | Gerenciador de compilação portátil |
| **JUnit 5 & Mockito** | Boot Starter Test | Suíte de testes automatizados com padrão AAA |

---

## 🏛 Estrutura do Projeto

```text
com.clyvo.veterinary/
├── config/                         ← Configurações e Infraestrutura
│   ├── DataInitializer.java        ← Seed de teste isolado por perfil (@Profile)
│   ├── InternacionalizacaoConfig.java ← Configuração de Locale e i18n
│   ├── JwtFilter.java              ← Filtro JWT com injeção de perfis RBAC
│   ├── JwtUtil.java                ← Assinatura HMAC-SHA e extração de claims
│   ├── SecurityConfig.java         ← Filtros, CSRF e políticas de sessão
│   └── SwaggerConfig.java          ← Configuração do Bearer Auth no Swagger
│
├── controllers/                    ← Controladores da Aplicação
│   ├── web/                        ← Camada Web MVC (Thymeleaf SSR)
│   │   ├── WebAuthController.java  ← Login e Logout Web com sessões
│   │   ├── WebTutorController.java ← Dashboard do Tutor, Pets e Agendamento
│   │   ├── WebVetController.java   ← Dashboard do Médico e Conclusão de Consultas
│   │   └── WebClinicaController.java ← Gestão de Corpo Clínico da Unidade
│   │
│   ├── AppointmentController.java  ← Endpoint de conclusão médica (Retrocompatibilidade)
│   ├── AutorizacaoController.java  ← Gestão e revogação de consentimento LGPD
│   ├── ClinicaController.java      ← Multi-tenancy, vínculos e transferências
│   ├── ConsultaController.java     ← Agendamento e cancelamento de consultas
│   ├── NotificacaoController.java  ← Caixa de entrada de notificações
│   ├── PetController.java          ← Gestão de pets por tutor
│   ├── RacaController.java         ← Catálogo padronizado de raças
│   ├── TutorController.java        ← Consulta e perfil de tutores
│   └── VeterinarioController.java  ← Catálogo e perfil de médicos veterinários
│
├── dto/                            ← Data Transfer Objects (Payloads REST)
├── exceptions/                     ← Hierarquia de Exceções de Domínio
│   ├── AccessDeniedException.java   ← HTTP 403 (Acesso Negado / Pet de outro tutor)
│   ├── AccountBlockedException.java ← HTTP 403 (Conta Bloqueada por Força Bruta)
│   ├── BusinessException.java       ← HTTP 400 (Datas passadas, conflito de agenda)
│   ├── ResourceNotFoundException.java ← HTTP 404 (Entidade não encontrada)
│   └── GlobalExceptionHandler.java  ← Interceptador global @RestControllerAdvice
│
├── models/                         ← Entidades de Domínio JPA (3NF)
│   ├── enums/                      ← Tipos fortemente tipados (TipoConta, StatusConsulta, etc.)
│   ├── Prontuario.java             ← Entidade de Prontuário Médico (3NF)
│   ├── Consulta.java               ← Evento de Agendamento
│   ├── Pet.java                    ← Animal vinculado ao Tutor
│   ├── Tutor.java / Veterinario.java / Clinica.java ← Perfis de Acesso
│   └── ...
│
├── repositories/                   ← Interfaces Spring Data JPA
│   ├── ProntuarioRepository.java   ← Consultas de histórico clínico
│   └── ConsultaRepository.java     ← Queries customizadas de conflito de agenda
│
└── services/                       ← Camada de Regras de Negócio
    ├── AuthService.java            ← Login com proteção anti-força bruta
    ├── ConsultaService.java        ← Validações de domínio e conclusão médica
    ├── AutorizacaoService.java     ← Governança de acesso LGPD
    └── ...
```

---

## 🗄 Banco de Dados e Migrations Flyway (Oracle ANSI SQL)

O versionamento é gerenciado integralmente pelo **Flyway** na pasta `src/main/resources/db/migration/`:

| Migration | Responsabilidade e Estrutura |
|---|---|
| `V1__Create_3NF_Core.sql` | Estrutura central: `conta_acesso`, `credencial`, `identificador_acesso`, perfis (`tutor`, `veterinario`, `clinica`), `veterinario_clinica`, `autorizacao_acesso_pet` e `sessao`. |
| `V2__Create_Racas.sql` | Catálogo normalizado de espécies e mais de 30 raças padronizadas. |
| `V3__Create_Consulta_And_Populate_Vets.sql` | Tabela `consulta` e carga inicial de médicos veterinários credenciados. |
| `V4__Create_Notificacao.sql` | Histórico e caixa de entrada de notificações de eventos (`notificacao`). |
| `V5__Add_Clinica_To_Consulta_And_Autorizacao.sql` | Vínculo multi-tenancy de `id_clinica` em `consulta` e autorizações de acesso. |
| `V6__Create_Prontuario.sql` | **Nova tabela `prontuario`** normalizada em 3NF com chave estrangeira para `consulta` e `veterinario`. |

### Adaptação para o Padrão Oracle (FIAP):
* Chaves primárias: `RAW(16) DEFAULT SYS_GUID()`.
* Booleanos: `NUMBER(1) CHECK (campo IN (0, 1))`.
* Textos: `VARCHAR2(4000)`.
* Remoção de cláusulas PostgreSQL incompatíveis (`DO $$`, `gen_random_uuid()`, `ON CONFLICT`).

---

## 🔒 Segurança, RBAC e Anti-Brute Force

1. **RBAC no JWT**: O `JwtFilter` extrai o tipo de conta contido no token e injeta no `SecurityContextHolder`:
   * `ROLE_TUTOR`: Restrito a gerenciar seus próprios pets e agendar consultas.
   * `ROLE_VETERINARIO`: Acesso à fila médica e conclusão de prontuários.
   * `ROLE_CLINICA`: Gestão do corpo clínico e transferências internas.
2. **Proteção contra Força Bruta**:
   * A cada tentativa incorreta de login, o contador `tentativas_falhas` na tabela `credencial` é incrementado.
   * Ao atingir **5 tentativas consecutivas**, o campo `bloqueado_ate` é preenchido com bloqueio de **15 minutos**.
   * Novas tentativas durante esse período retornam `403 Forbidden` (`AccountBlockedException`).
3. **Prevenção contra Enumeração de Usuários**: Mensagem unificada e genérica (*"E-mail ou senha incorretos."*) tanto para usuário inexistente quanto para senha incorreta.
4. **Proteção de Credenciais**:
   * Variáveis de ambiente `${DB_URL}`, `${DB_USER}`, `${DB_PASS}` e `${JWT_SECRET}`.
   * O `DataInitializer` possui a anotação `@Profile({"local", "dev"})`, garantindo que contas de teste com senhas previsíveis jamais sejam geradas no banco da faculdade.

---

## 🖥 Interface Web Thymeleaf e Internacionalização (i18n)

Acesse pelo navegador em: **`http://localhost:8080/`**

* **Design System**: Interface com visual moderno **Aurora Glassmorphism**, contraste acessível, modais de confirmação e responsividade.
* **Padrão Post-Redirect-Get (PRG)**: Todos os formulários (cadastro de pet, agendamento de consulta, atendimento) utilizam redirecionamento HTTP 302 com `RedirectAttributes`, prevenindo reenvio duplicado de dados ao pressionar F5.
* **Internacionalização (i18n)**:
  * Suporte nativo a Português (`pt_BR`) e Inglês (`en`).
  * Alternância dinâmica através do seletor no cabeçalho ou pelo parâmetro de URL:
    * `http://localhost:8080/?lang=pt_BR`
    * `http://localhost:8080/?lang=en`

---

## 📡 Endpoints da API REST (Compatibilidade Mobile)

Documentação Swagger interativa: **`http://localhost:8080/swagger-ui.html`**

| Recurso | Método & Rota | Descrição |
|---|---|---|
| **Autenticação** | `POST /api/auth/register` | Registro de novos usuários com validação de perfil. |
| | `POST /api/auth/login` | Autenticação, verificação de bloqueio e emissão de JWT. |
| **Pets** | `GET /api/pets` | Listagem dos animais pertencentes ao tutor autenticado. |
| | `POST /api/pets` | Cadastro de novo pet vinculado ao tutor autenticado. |
| **Consultas** | `POST /api/consultas` | Agendamento com validação de posse do pet, data e conflito médico. |
| | `GET /api/consultas` | Listagem filtrada por perfil (tutor, veterinário ou clínica). |
| | `PUT /api/consultas/{id}/cancelar` | Cancelamento com notificação automática às partes. |
| **Atendimento** | `POST /api/appointments/{id}/complete` | Conclusão de consulta e **persistência de Prontuário**. |
| **Clínicas** | `GET /api/clinicas` | Catálogo de clínicas ativas para agendamento. |
| | `GET /api/clinicas/{id}/veterinarios` | Veterinários ativos com vínculo na clínica. |
| | `POST /api/clinicas/veterinarios/vincular` | Vinculação de médico à equipe clínica. |
| | `PUT /api/clinicas/autorizacoes/{id}/transferir` | Transferência de prontuário e consultas entre veterinários. |
| **Autorizações** | `GET /api/autorizacoes` | Consulta de consentimentos de prontuário ativos (LGPD). |
| | `PUT /api/autorizacoes/{id}/revogar` | Revogação de acesso pelo tutor com cancelamento em cascata. |
| **Notificações**| `GET /api/notificacoes` | Caixa de entrada de notificações em ordem cronológica. |

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java 21 LTS** ou superior instalado.
* Acesso ao terminal com o Maven Wrapper (`./mvnw`).

### 1. Execução Local (Modo Recomendado / Demonstração)
O perfil `local` utiliza o banco H2 em memória em modo de compatibilidade Oracle (`MODE=Oracle`), executa automaticamente todas as migrations do Flyway e popula as contas de demonstração:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 2. Execução com Oracle Database da FIAP
Configure as variáveis de ambiente com as credenciais acadêmicas e execute:

```bash
export DB_URL="jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL"
export DB_USER="seu_rm"
export DB_PASS="sua_senha"
export JWT_SECRET="chave_segura_de_no_minimo_32_caracteres"

./mvnw spring-boot:run
```

### 3. Credenciais Pré-Cadastradas para Teste (Perfil Local)

| Perfil | E-mail | Senha | Funcionalidades no Dashboard |
|---|---|---|---|
| **Tutor** | `ana.silva@email.com` | `senha123` | Cadastrar pets, agendar consultas e revogar autorizações. |
| **Veterinário** | `dr.carlos@veterinaria.com` | `senha123` | Fila médica, preencher notas clínicas e registrar prontuários. |
| **Clínica** | `contato@matriz.com` | `senha123` | Corpo clínico credenciado e transferência de atendimentos. |

---

## 🧪 Testes Automatizados (Padrão AAA)

A aplicação conta com uma suíte de **28 testes automatizados** utilizando **JUnit 5** e **Mockito**, estruturados no padrão **Arrange, Act, Assert (AAA)**:

```bash
./mvnw clean test
```

### Cobertura de Cenários Críticos:
* **`ConsultaServiceTest`**: Rejeição de agendamento de pet alheio (403), rejeição de datas retroativas (400), prevenção de conflito de agenda médica (400), agendamento válido e persistência do Prontuário na conclusão do atendimento.
* **`AuthServiceTest`**: Bloqueio de conta por tentativas consecutivas incorretas (anti-força bruta), autenticação válida com JWT, rejeição de status inativo e prevenção de e-mails duplicados.
* **`PetServiceTest`**: Validação de vínculo e persistência de animais.
* **`ConsultaControllerTest`, `ClinicaControllerTest`, `AutorizacaoControllerTest`**: Validações de camada HTTP e conformidade de status codes.
* **`ClyvoVeterinaryApplicationTests`**: Validação de integridade do contexto e schema relacional.

```text
[INFO] Results:
[INFO] Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 👨‍💻 Autor

Desenvolvido por **Maicon Douglas**:
* **RM:** 561279
* **GitHub:** [@MaiconDouglas-dev](https://github.com/MaiconDouglas-dev)
* **E-mail:** [maicon.timot8@gmail.com](mailto:maicon.timot8@gmail.com)

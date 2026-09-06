# 🐾 Clyvo Veterinary API (`api-clyvovet_M`)

> API RESTful e Plataforma Web para gestão de clínicas, médicos veterinários e tutores de pets, desenvolvida com **Spring Boot 3.5**, **Java**, **PostgreSQL** e modelagem normalizada em **3NF**.

![Java](https://img.shields.io/badge/Java-21%20%2F%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%2B-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-Migrations-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-JJWT_0.12.6-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

---

## 📌 Sumário

* [Visão Geral](#-visão-geral)
* [Tecnologias Utilizadas](#-tecnologias-utilizadas)
* [Arquitetura do Projeto](#-arquitetura-do-projeto)
* [Banco de Dados e Modelagem 3NF](#-banco-de-dados-e-modelagem-3nf)
* [Endpoints da API](#-endpoints-da-api)
* [Frontend SPA Integrado](#-frontend-spa-integrado)
* [Como Executar o Projeto](#-como-executar-o-projeto)
* [Testes Automatizados](#-testes-automatizados)
* [Autor](#-autor)

---

## 📖 Visão Geral

O **Clyvo Veterinary** é um sistema completo para o ecossistema veterinário, permitindo a interação harmoniosa entre:
* **Tutores:** Cadastro de animais de estimação, seleção de raças padronizadas, agendamento de consultas médicas (online ou presenciais) e visualização de notificações e receitas.
* **Médicos Veterinários:** Gestão da fila de consultas agendadas, atendimento clínico e registro de notas médicas e orientações.
* **Clínicas:** Estrutura preparada para vínculo profissional e administração de estabelecimentos.

A autenticação é protegida por tokens **JWT Stateless** com registro de auditoria e revogação de sessões em banco de dados.

---

## 🛠 Tecnologias Utilizadas

| Tecnologia | Versão | Papel no Sistema |
|---|---|---|
| **Java** | 21 LTS / 25 | Linguagem principal de desenvolvimento |
| **Spring Boot** | 3.5.4 | Framework base (Web, Security, Data JPA, Validation) |
| **Spring Security** | 6.x | Controle de autenticação e proteção de rotas |
| **JJWT (io.jsonwebtoken)** | 0.12.6 | Geração, assinatura (HMAC-SHA256) e validação de tokens JWT |
| **Spring Data JPA / Hibernate** | 6.6.x | Mapeamento Objeto-Relacional (ORM) |
| **PostgreSQL** | 16+ | Banco de dados relacional principal |
| **Flyway** | 10.x | Controle versionado de migrações e população de dados |
| **SpringDoc OpenAPI** | 2.8.5 | Documentação interativa Swagger UI em `/swagger-ui.html` |
| **Maven Wrapper (`mvnw`)** | 3.9.16 | Gerenciador de compilação e dependências portátil |
| **JUnit 5 & Mockito** | (via Boot) | Testes unitários e testes de integração de contexto |

> **Nota de Design:** O projeto não utiliza Lombok. Todas as classes utilizam POJOs em Java puro, garantindo transparência em stack traces e total compatibilidade entre compiladores.

---

## 🏛 Arquitetura do Projeto

O projeto adota o padrão **MVC em Camadas (Layered Architecture)** com separação estrita de responsabilidades:

```
com.clyvo.veterinary/
│
├── config/                  ← Infraestrutura e Configurações
│   ├── GlobalExceptionHandler.java  ← Tratamento global de erros da API
│   ├── JwtFilter.java               ← Filtro de interceptação de tokens nas requisições
│   ├── JwtUtil.java                 ← Assinatura e validação do JWT com chave persistente
│   ├── SecurityConfig.java          ← Configuração de rotas públicas/privadas e CORS/CSRF
│   └── SwaggerConfig.java           ← Configuração do esquema Bearer no OpenAPI
│
├── controllers/             ← [CONTROLLER] Exposição dos Endpoints REST
│   ├── AuthController.java          ← Registro e login de usuários
│   ├── PetController.java           ← CRUD e listagem de pets por tutor
│   ├── ConsultaController.java      ← Agendamento e listagem de consultas
│   ├── AppointmentController.java   ← Conclusão de atendimentos clínicos
│   ├── VeterinarioController.java   ← Catálogo de médicos veterinários
│   ├── TutorController.java         ← Listagem de tutores
│   ├── RacaController.java          ← Catálogo padronizado de raças
│   └── NotificacaoController.java   ← Caixa de entrada de notificações
│
├── dto/                     ← Objetos de Transferência de Dados (Payloads)
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── RegisterRequest.java
│   ├── ScheduleAppointmentRequest.java
│   └── CompleteAppointmentRequest.java
│
├── models/                  ← [MODEL] Entidades de Domínio JPA (3NF)
│   ├── ContaAcesso.java
│   ├── Credencial.java
│   ├── IdentificadorAcesso.java
│   ├── Sessao.java
│   ├── Tutor.java
│   ├── Veterinario.java
│   ├── Clinica.java
│   ├── VeterinarioClinica.java
│   ├── AutorizacaoAcessoPet.java
│   ├── Especie.java
│   ├── Raca.java
│   ├── Pet.java
│   ├── Consulta.java
│   └── Notificacao.java
│
├── repositories/            ← [DATA] Interfaces Spring Data JPA
│
└── services/                ← [SERVICE] Regras de Negócio e Casos de Uso
    └── AuthService.java
```

---

## 🗄 Banco de Dados e Modelagem 3NF Aprimorada

O banco de dados segue rigorosamente a **Terceira Forma Normal (3NF)** e a topologia de relacionamento:

```
                          ROLE (Perfil)
                          │
                          ▼
                        CONTA (ContaAcesso)
                          │
             ┌────────────┼────────────┐
             │            │            │
             ▼            ▼            ▼
           TUTOR      VETERINARIO    CLINICA
             │            │            ▲
             ▼            │            │
            PET           │   VETERINARIO_CLINICA
             │            │
             │            │
             ├──── AUTORIZACAO_PET
             │            │
             ▼            │
          CONSULTA ◄──────┘
```

| Migração | Conteúdo e Responsabilidade |
|---|---|
| `V1__Create_3NF_Core.sql` | Estrutura de contas (`conta_acesso`), credenciais (`credencial`), documentos (`identificador_acesso`), perfis (`tutor`, `veterinario`, `clinica`), vínculos (`veterinario_clinica`), autorizações (`autorizacao_acesso_pet`) e sessões. |
| `V2__Create_Racas.sql` | Normalização de espécies (`especie`) e catálogo com mais de 30 raças com IDs padronizados. |
| `V3__Create_Consulta_And_Populate_Vets.sql` | Tabela de agendamento de consultas (`consulta`) e seed com veterinários padrão (Dr. Roberto Silveira, Dra. Camila Nogueira, Dr. Marcos Santos). |
| `V4__Create_Notificacao.sql` | Histórico e caixa de entrada de notificações em tempo real (`notificacao`). |
| `V5__Add_Clinica_To_Consulta_And_Autorizacao.sql` | Vínculo estrito de `id_clinica` em `consulta` e `autorizacao_acesso_pet`, multi-tenancy e seed de clínicas e vínculos. |

> Todas as chaves primárias são do tipo `UUID` geradas via `gen_random_uuid()` no PostgreSQL.

---

## 📡 Endpoints da API

Acesse a documentação interativa com execução em tempo real via Swagger UI:
👉 **`http://localhost:8080/swagger-ui.html`**

### 1. Autenticação (`/api/auth`)
* `POST /api/auth/register` — Cadastra uma nova conta (`TUTOR`, `VETERINARIO` ou `CLINICA`).
* `POST /api/auth/login` — Autentica as credenciais, gera uma sessão e retorna o token JWT.

### 2. Clínicas e Corpo Clínico (`/api/clinicas`)
* `GET /api/clinicas` — Lista todas as clínicas ativas para seleção pública de agendamento.
* `GET /api/clinicas/{id}/veterinarios` — Lista os veterinários ativos de uma clínica específica.
* `GET /api/clinicas/minha` — Retorna os dados da clínica autenticada.
* `GET /api/clinicas/meus-veterinarios` — Lista a equipe médica da clínica autenticada com status de vínculo.
* `POST /api/clinicas/veterinarios/vincular` — Vincula um médico veterinário à equipe da clínica.
* `PUT /api/clinicas/veterinarios/{idVinculo}/desvincular` — Desativa o vínculo de um médico com a clínica.
* `GET /api/clinicas/consultas` — Lista todas as consultas da unidade (isolamento multi-tenancy).
* `GET /api/clinicas/autorizacoes` — Lista as autorizações de acesso ativas para pacientes da unidade.
* `PUT /api/clinicas/autorizacoes/{id}/transferir` — Transfere o atendimento e consultas agendadas de um pet para outro colega médico da mesma clínica.

### 3. Autorizações de Acesso (`/api/autorizacoes`)
* `GET /api/autorizacoes` — Lista autorizações ativas (filtradas por perfil: tutor, veterinário ou clínica).
* `PUT /api/autorizacoes/{id}/revogar` — Tutor revoga o consentimento de acesso ao pet, cancelando automaticamente agendamentos futuros.

### 4. Animais (`/api/pets`)
* `POST /api/pets` — Cadastra um animal vinculado ao tutor logado.
* `GET /api/pets` — Lista todos os animais do tutor logado.

### 5. Consultas e Atendimentos (`/api/consultas` e `/api/appointments`)
* `POST /api/consultas` — Agenda uma consulta entre um pet, veterinário e clínica (cria automaticamente a `AutorizacaoAcessoPet`).
* `GET /api/consultas` — Lista as consultas do usuário logado (filtro automático: tutor, veterinário ou isolamento de clínica).
* `PUT /api/consultas/{id}/cancelar` (ou `POST` / `DELETE`) — Cancela uma consulta agendada e notifica tutor e equipe.
* `POST /api/appointments/{id}/cancel` — Endpoint alternativo de cancelamento.
* `POST /api/appointments/{id}/complete` — Finaliza o atendimento, altera status para `CONCLUIDA` e registra observações clínicas.

### 6. Notificações (`/api/notificacoes`)
* `GET /api/notificacoes` — Retorna as notificações em ordem decrescente (da mais recente para a mais antiga).

---

## 💻 Frontend SPA Integrado

O projeto conta com uma interface nativa (Single Page Application) em HTML5, CSS3 e JavaScript Vanilla localizada em `src/main/resources/static/`:

* **`login.html`**: Formulário responsivo com comutação de campos (CRMV condicional para Veterinário, CNPJ para Clínica).
* **`dashboard-tutor.html`**: Painel do tutor com gestão de pets, agendamento hierárquico (Clínica -> Veterinário da Unidade -> Pet -> Data), fila de consultas e controle de autorizações com dupla confirmação e alertas de consequências.
* **`dashboard-clinica.html`**: Painel exclusivo da clínica com gestão de corpo clínico, consultas da unidade e transferência de atendimento entre veterinários.
* **`dashboard-vet.html`**: Painel do veterinário com fila de consultas agendadas e finalização com anotações clínicas.
* **`app.js`**: Gerenciador de requisições assíncronas (`fetch`) com armazenamento de JWT em `localStorage`.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
* **Java 21 ou 25** instalado
* **PostgreSQL 16+** rodando localmente

### 1. Configurar o Banco de Dados
No terminal PostgreSQL (psql), crie a base e o usuário:
```sql
CREATE USER postgres WITH PASSWORD 'postgres' SUPERUSER;
CREATE DATABASE clyvovet OWNER postgres;
```

### 2. Executar a Aplicação
Clone o repositório e inicie utilizando o **Maven Wrapper** incluso:
```bash
git clone https://github.com/MCGI-PW/API-Clyvo-M_Vet.git
cd API-Clyvo-M_Vet

./mvnw spring-boot:run
```

* O **Flyway** executará automaticamente todas as migrações e criará as tabelas e dados iniciais.
* O servidor subirá na porta padrão **8080**.

### 3. Acessar
* **Frontend Web:** [http://localhost:8080/login.html](http://localhost:8080/login.html)
* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 🧪 Testes Automatizados

O projeto contém testes unitários e de integração prontos para execução:

```bash
./mvnw test
```

* **`AuthServiceTest`:** Testes unitários com Mockito cobrindo registro de tutores e veterinários, prevenção de e-mails duplicados, verificação de credenciais válidas e rejeição de senhas incorretas.
* **`ClyvoVeterinaryApplicationTests`:** Teste de integração garantindo o carregamento completo do contexto do Spring Boot e a integridade do schema relacional.

---

## 👨‍💻 Autor

Desenvolvido e mantido por **Maicon Douglas**:
* **GitHub:** [@MaiconDouglas-dev](https://github.com/MaiconDouglas-dev)
* **E-mail:** [maicon.timot8@gmail.com](mailto:maicon.timot8@gmail.com)

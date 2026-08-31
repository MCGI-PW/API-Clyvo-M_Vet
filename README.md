# API-Clyvo-M_Vet

API REST e Web para o sistema veterinário Clyvo, desenvolvida com Spring Boot 3.5 e Java 25.

O sistema permite que **médicos veterinários** criem e gerenciem seu perfil profissional, e que **tutores** (donos de pets) cadastrem seus animais e agendem consultas. A autenticação suporta login tradicional com e-mail e senha, e login social via Google OAuth2.

---

## Tecnologias

| Tecnologia | Versão | Papel |
|---|---|---|
| Java | 25 LTS | Linguagem principal |
| Spring Boot | 3.5.4 | Framework base |
| Spring Security 6 | (via Boot) | Autenticação e autorização |
| Spring OAuth2 Client | (via Boot) | Login com Google |
| JJWT | 0.12.6 | Geração e validação de tokens JWT |
| Spring Data JPA | (via Boot) | Persistência com Hibernate |
| PostgreSQL | (runtime) | Banco de dados principal |
| Flyway | (via Boot) | Controle de versão do schema |
| Thymeleaf | (via Boot) | Templates HTML do frontend web |
| thymeleaf-extras-springsecurity6 | (via Boot) | Integração Thymeleaf com Spring Security |
| springdoc-openapi | 2.6.0 | Documentação Swagger UI em `/swagger-ui.html` |
| H2 | (test scope) | Banco em memória para testes |
| Spring Boot DevTools | (runtime/dev) | Reload automático em desenvolvimento |
| Maven | 3.9.x | Gerenciador de build |

> **Nota:** O projeto não utiliza Lombok. Todo boilerplate (construtores, getters, setters, builders) é escrito em Java puro.

---

## Arquitetura

O projeto segue a **Arquitetura Hexagonal** (Ports & Adapters), combinada com os princípios **SOLID**.

Cada módulo de domínio é independente e organizado em três camadas:

```
com.clyvo.veterinary.{modulo}
│
├── domain/              ← Regras de negócio puras (sem framework)
│   ├── model/           ← Entidades e Value Objects de domínio
│   ├── repository/      ← Interfaces (ports de saída)
│   └── service/         ← Serviços de domínio (quando existem)
│
├── application/         ← Casos de uso (orquestração da lógica)
│   ├── dto/             ← Objetos de transferência de dados
│   ├── mapper/          ← Conversão DTO ↔ Domínio
│   ├── port/in/         ← Interfaces de entrada (use cases)
│   └── service/         ← Implementação dos use cases
│
└── infrastructure/      ← Adaptadores e detalhes técnicos
    ├── persistence/
    │   ├── entity/      ← Entidades JPA (@Entity)
    │   ├── mapper/      ← Conversão Entity ↔ Domain
    │   ├── repository/  ← Interfaces Spring Data JPA
    │   └── adapter/     ← Implementação dos ports de saída
    └── presentation/
        ├── rest/        ← Controllers REST (@RestController) — para clientes mobile/API
        └── web/         ← Controllers Web (@Controller) — para o frontend Thymeleaf
```

A camada `shared` contém componentes transversais usados por todos os módulos:

```
shared/
├── application/security/    ← @CurrentUser (meta-anotação)
├── domain/exception/        ← BusinessException, ResourceNotFoundException
├── infrastructure/config/   ← SecurityConfig, JacksonConfig
└── presentation/error/      ← ErrorResponse, GlobalExceptionHandler
```

---

## Módulos de Domínio

O sistema é composto por 9 módulos de domínio:

### `user`
Representa um usuário da plataforma. Todo acesso ao sistema começa por aqui.

- **Modelo:** `User` com campos `id`, `name`, `email`, `passwordHash`, `role`, `active`, `googleId`, `createdAt`, `updatedAt`
- **Role (enum):** `ROLE_VETERINARIAN`, `ROLE_TUTOR`, `ROLE_ADMIN`
- **Criação:** Três métodos de fábrica estáticos: `User.create()`, `User.createWithGoogle()`, `User.load()`
- A senha é armazenada como hash BCrypt. Usuários Google não têm `passwordHash`

### `auth`
Responsável por registro e login. Implementa `UserDetailsService` para integração com Spring Security.

- **Registro:** valida e-mail único, codifica senha com BCrypt, persiste o usuário
- **Login:** delega autenticação ao `AuthenticationManager`, gera token JWT via `JwtService`
- **JWT:** gerado com JJWT 0.12.6, assinado com HS256, expira em 24 horas (configurável via `jwt.expiration.ms`)
- **OAuth2:** `OAuth2SuccessHandler` intercepta o sucesso do login Google, cria ou recupera o `User`, gera JWT e redireciona
- **Filtro:** `JwtAuthenticationFilter` intercepta todas as requisições `POST /api/**`, extrai e valida o token

### `veterinarian`
Perfil profissional do médico veterinário, vinculado a um `User`.

- **Modelo:** `Veterinarian` com `userId`, `crm`, `specialty`, `bio`, `phone`, `profilePictureUrl`, `subscriptionPlan`, `subscriptionStatus`
- **Specialty (enum):** `GENERAL_PRACTICE`, `SURGERY`, `DERMATOLOGY`, `CARDIOLOGY`, `ONCOLOGY`, `ORTHOPEDICS`, `NEUROLOGY`, `OPHTHALMOLOGY`, `DENTISTRY`, `EXOTIC_ANIMALS`
- **SubscriptionPlan (enum):** `FREE`, `BASIC`, `PREMIUM`
- **Regras:** CRM deve ser único; o `User` vinculado deve ter `role == ROLE_VETERINARIAN`
- **Use Case:** `createProfile`, `updateProfile`, `getProfile`, `getProfileByUserId`, `listAll`, `deleteProfile`

### `tutor`
Perfil do dono do pet, vinculado a um `User`.

- **Modelo:** `Tutor` com `userId`, `phone`, `address`, `profilePictureUrl`
- **Regras:** Um `User` só pode ter um perfil de tutor

### `pet`
Representa um animal de estimação, sempre vinculado a um `Tutor`.

- **Modelo:** `Pet` com `tutorId`, `name`, `species`, `breed`, `birthDate`, `observations`
- **Species (enum):** `DOG`, `CAT`, `BIRD`, `RABBIT`, `FISH`, `REPTILE`, `RODENT`, `OTHER`

### `appointment`
Gerencia o agendamento de consultas entre tutores e veterinários.

- **Modelo:** `Appointment` com `petId`, `veterinarianId`, `tutorId`, `scheduledAt`, `status`, `notes`, `finalNotes`
- **AppointmentStatus (enum):** `SCHEDULED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- **AppointmentDomainService:** Serviço de domínio (`@Component`) que encapsula regras como validação de conflito de horário

### `medicalrecord`
Prontuário eletrônico de um pet gerado após uma consulta.

- **Modelo:** `MedicalRecord` com `petId`, `veterinarianId`, `appointmentId`, `diagnosis`, `treatment`, `observations`, `recordDate`

### `prescription`
Prescrição médica emitida por um veterinário.

- **Modelo:** `Prescription` com lista de `PrescriptionItem` (medicamento, dosagem, frequência, duração)
- **`PrescriptionItem`** é um Value Object embarcado (`@Embeddable`)
- **Endpoints REST reais:**
  - `POST /api/prescriptions` — cria prescrição (somente `ROLE_VETERINARIAN`)
  - `GET /api/prescriptions/{id}` — busca por ID (`VETERINARIAN` ou `ADMIN`)
  - `GET /api/prescriptions/pet/{petId}` — lista por pet (`VETERINARIAN` ou `TUTOR`)
  - `GET /api/prescriptions/vet/me` — lista as prescrições do vet autenticado
  - `DELETE /api/prescriptions/{id}` — remove prescrição (somente `ADMIN`)

### `vaccine`
Registro do histórico de vacinas de um pet, com controle da próxima dose.

- **Modelo:** `Vaccine` com `petId`, `veterinarianId`, `name`, `appliedAt`, `nextDoseAt`, `batch`, `manufacturer`

### `integration`
Adaptador de comunicação com o sistema orquestrador .NET (backend da plataforma Chronos).

- **`OrchestratorPort`:** interface (port de saída) que define o contrato
- **`OrchestratorHttpAdapter`:** implementação que realiza chamadas HTTP usando `RestTemplate`; configurável via `orchestrator.base-url` e `orchestrator.api-key`

---

## Segurança

A segurança é configurada em `SecurityConfig` com **duas cadeias de filtros independentes**:

### Cadeia 1 — API REST (`@Order(1)`, matcher `/api/**`)
- **CSRF:** desabilitado (clientes mobile/REST não usam cookies de sessão)
- **Sessão:** `STATELESS` — nenhuma sessão HTTP é criada
- **Rotas públicas:** `/api/auth/**` (registro e login)
- **Todas as demais:** exigem token JWT válido no header `Authorization: Bearer <token>`
- **Filtro:** `JwtAuthenticationFilter` é executado antes do `UsernamePasswordAuthenticationFilter`

### Cadeia 2 — Frontend Web (`@Order(2)`, sem matcher — cobre tudo que não é `/api/**`)
- **Sessão:** stateful (padrão do Spring Security)
- **Rotas públicas:** `/login`, `/register`, `/css/**`, `/js/**`, `/images/**`, `/oauth2/**`, `/login/oauth2/**`
- **Form login:** página customizada em `/login`, redireciona para `/` após sucesso
- **OAuth2 Google:** página de login em `/login`, após sucesso chama `OAuth2SuccessHandler`
- **Logout:** redireciona para `/login?logout`

**Autorização por método** está habilitada via `@EnableMethodSecurity`. Os controllers usam `@PreAuthorize("hasRole('VETERINARIAN')")` para controle granular.

---

## Banco de Dados

O schema é gerenciado pelo **Flyway** e versionado em `src/main/resources/db/migration/`.

| Migration | Tabela criada |
|---|---|
| `V1__create_users_table.sql` | `users` |
| `V2__create_veterinarians_table.sql` | `veterinarians` |
| `V3__create_tutors_table.sql` | `tutors` |
| `V4__create_pets_table.sql` | `pets` |
| `V5__create_appointments_table.sql` | `appointments` |
| `V6__create_medical_records_table.sql` | `medical_records` |
| `V7__create_prescriptions_table.sql` | `prescriptions` |
| `V8__create_vaccines_table.sql` | `vaccines` |
| `V9__insert_admin_user.sql` | (seed do usuário admin padrão) |

Todas as PKs são `UUID` geradas pelo banco com `gen_random_uuid()`.

O Hibernate está configurado com `ddl-auto=none` — ele nunca altera o schema. O Flyway é a única fonte de verdade para o schema.

---

## Frontend Web (Thymeleaf)

O sistema possui uma interface web completa para uso no navegador, com templates organizados por perfil:

```
src/main/resources/templates/
├── layouts/
│   └── base.html         ← Layout base com sidebar, topbar e tokens CSS
├── auth/
│   ├── login.html        ← Formulário de login + botão Google OAuth2
│   └── register.html     ← Formulário de cadastro com seleção de tipo de conta
├── vet/
│   ├── dashboard.html    ← Dashboard do veterinário
│   ├── profile.html      ← Visualização do perfil profissional
│   ├── profile-edit.html ← Edição de perfil (especialidade, bio, telefone)
│   └── appointments.html ← Lista de consultas com ações confirmar/concluir
├── tutor/
│   ├── dashboard.html    ← Dashboard do tutor com resumo de pets e consultas
│   ├── profile.html      ← Perfil do tutor
│   ├── pets.html         ← Grid de cards com os pets cadastrados
│   └── appointments.html ← Lista de consultas com ação cancelar
├── admin/
│   ├── dashboard.html    ← Dashboard administrativo
│   └── users.html        ← Gestão de usuários
└── error/
    ├── 403.html          ← Página de acesso negado
    └── 404.html          ← Página não encontrada
```

O CSS global está em `src/main/resources/static/css/main.css` e define um design system com variáveis de cor, componentes de card, tabela, badge, botão e sidebar.

---

## Configuração

Todas as configurações sensíveis leem variáveis de ambiente com valor padrão de fallback para desenvolvimento local.

### `application.properties`

```properties
# Servidor
server.port=${PORT:8080}

# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/clyvovet_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

# JWT
jwt.secret=${JWT_SECRET:clyvovet-super-secret-key-2026-must-be-at-least-256-bits-long-for-hs256}
jwt.expiration.ms=${JWT_EXPIRATION_MS:86400000}

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:SEU_GOOGLE_CLIENT_ID_AQUI}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:SEU_GOOGLE_CLIENT_SECRET_AQUI}

# Integração .NET
orchestrator.base-url=${ORCHESTRATOR_URL:http://localhost:5000}
orchestrator.api-key=${ORCHESTRATOR_API_KEY:dev-api-key}
```

### Para desenvolvimento local

1. Crie um banco PostgreSQL chamado `clyvovet_db`
2. Para OAuth2 com Google, crie credenciais no [Google Cloud Console](https://console.cloud.google.com/apis/credentials) e defina as variáveis `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET`
3. O Flyway criará todas as tabelas automaticamente na primeira inicialização

---

## Como Executar

### Pré-requisitos

- Java 25
- Maven 3.9+
- PostgreSQL 15+ rodando localmente

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/MCGI-PW/API-Clyvo-M_Vet.git
cd API-Clyvo-M_Vet

# 2. Configure o banco (o Flyway faz o resto)
# Crie o banco: CREATE DATABASE clyvovet_db;

# 3. Execute (com variáveis de ambiente ou com os valores default)
mvn spring-boot:run

# 4. Acesse
# Frontend web:  http://localhost:8080/login
# Swagger UI:    http://localhost:8080/swagger-ui.html
# API docs JSON: http://localhost:8080/v3/api-docs
```

---

## Testes

O projeto contém testes unitários com **JUnit 5** e **Mockito**. Para testes, o Spring Boot usa H2 em memória (definido em `src/test/resources/application-test.properties`).

```bash
mvn test
```

Testes implementados:

- **`AuthServiceTest`** — cobre registro (e-mail disponível, e-mail duplicado) e login (credenciais válidas, credenciais inválidas)
- **`VeterinarianServiceTest`** — cobre criação de perfil (CRM disponível, CRM duplicado, usuário inexistente, usuário com role errada) e busca de perfil (ID existente, ID inexistente)

---

## Estrutura de Commits

O histórico de commits segue o padrão **Conventional Commits**:

```
chore: initial project setup with Spring Boot 3.5.4 and Java 25
feat(shared): add cross-cutting infrastructure module
feat(user): add user domain with Role enum and JPA adapter
feat(auth): add authentication with JWT and Google OAuth2
feat(veterinarian): add veterinarian profile with CRM and subscription plans
feat(tutor): add tutor profile module
feat(pet): add pet management with species classification
feat(appointment): add appointment scheduling with domain service validation
feat(medicalrecord): add medical record module
feat(prescription): add prescription module with REST controller
feat(vaccine): add vaccine tracking with next-dose calculation
feat(integration): add orchestrator port and HTTP adapter for .NET communication
feat(db): add Flyway migrations V1-V9 for all domain tables
feat(frontend): add global CSS design system with sidebar and component tokens
feat(frontend): add auth templates (login and register)
feat(frontend): add base layout fragment with sidebar navigation
feat(frontend): add veterinarian templates (dashboard, profile, edit, appointments)
feat(frontend): add tutor templates (dashboard, profile, pets, appointments)
feat(frontend): add admin templates (dashboard, users management)
feat(frontend): add error pages (403 Forbidden, 404 Not Found)
test: add unit tests for AuthService and VeterinarianService
```

---

## Decisões de Projeto

**Arquitetura Hexagonal:** A separação entre domínio, aplicação e infraestrutura garante que as regras de negócio não dependem de framework. É possível trocar o banco de dados ou o mecanismo de autenticação sem alterar o domínio.

**Dois filter chains separados:** A API REST usa JWT stateless para servir clientes mobile. O frontend web usa sessão stateful com form login e OAuth2. Ambos coexistem sem conflito graças ao `@Order` e ao `securityMatcher`.

**Sem Lombok:** Todo o código é Java puro, sem geração de código em tempo de compilação. Isso melhora a rastreabilidade em stack traces e torna o código mais explícito.

**Flyway como única fonte de verdade:** O Hibernate com `ddl-auto=none` nunca altera o schema. Toda mudança estrutural no banco passa por uma migration versionada e rastreável.

**UUID como PK:** Todas as entidades usam `UUID` gerado pelo banco com `gen_random_uuid()`, eliminando dependência de sequências e facilitando eventual sharding ou replicação.

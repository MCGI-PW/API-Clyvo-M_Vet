# 🗄️ DER: Modelo de Dados do Clyvo Vet

Diagrama gerado a partir das migrations Flyway [`V1..V6`](../src/main/resources/db/migration) (Oracle, 3ª Forma Normal).
O GitHub renderiza o diagrama Mermaid abaixo automaticamente.

```mermaid
erDiagram
    CONTA_ACESSO ||--o{ IDENTIFICADOR_ACESSO : "possui"
    CONTA_ACESSO ||--|| CREDENCIAL : "autentica com"
    CONTA_ACESSO ||--o{ SESSAO : "abre"
    CONTA_ACESSO ||--o{ CONTA_PERFIL : "recebe"
    PERFIL ||--o{ CONTA_PERFIL : "atribuído em"
    PERFIL ||--o{ PERFIL_PERMISSAO : "concede"
    PERMISSAO ||--o{ PERFIL_PERMISSAO : "faz parte de"
    CONTA_ACESSO ||--o| TUTOR : "é"
    CONTA_ACESSO ||--o| VETERINARIO : "é"
    CONTA_ACESSO ||--o| CLINICA : "é"
    CONTA_ACESSO ||--o| ADMIN_CLINICA : "é"
    CONTA_ACESSO ||--o{ NOTIFICACAO : "recebe"
    CLINICA ||--o{ ADMIN_CLINICA : "administrada por"
    VETERINARIO ||--o{ REGISTRO_VETERINARIO : "registra CRMV"
    VETERINARIO ||--o{ VETERINARIO_CLINICA : "vincula-se"
    CLINICA ||--o{ VETERINARIO_CLINICA : "contrata"
    TUTOR ||--o{ PET : "é responsável por"
    ESPECIE ||--o{ RACA : "classifica"
    RACA ||--o{ PET : "define"
    PET ||--o{ CONSULTA : "é atendido em"
    VETERINARIO ||--o{ CONSULTA : "realiza"
    CLINICA |o--o{ CONSULTA : "sedia"
    CONSULTA ||--o| PRONTUARIO : "gera"
    VETERINARIO ||--o{ PRONTUARIO : "assina"
    PET ||--o{ AUTORIZACAO_ACESSO_PET : "tem acesso controlado por"
    VETERINARIO ||--o{ AUTORIZACAO_ACESSO_PET : "é autorizado em"
    CLINICA |o--o{ AUTORIZACAO_ACESSO_PET : "é autorizada em"
    CONTA_ACESSO ||--o{ AUTORIZACAO_ACESSO_PET : "autoriza (tutor)"

    CONTA_ACESSO {
        RAW16 id_conta PK
        VARCHAR2 tipo_conta
        VARCHAR2 email UK
        VARCHAR2 telefone
        VARCHAR2 status_conta
        TIMESTAMP data_criacao
        TIMESTAMP data_atualizacao
    }
    IDENTIFICADOR_ACESSO {
        RAW16 id_identificador PK
        RAW16 id_conta FK
        VARCHAR2 tipo_identificador "CPF, CNPJ..."
        VARCHAR2 valor_hash
        VARCHAR2 valor_criptografado
        NUMBER1 ativo
    }
    CREDENCIAL {
        RAW16 id_credencial PK
        RAW16 id_conta FK,UK
        VARCHAR2 senha_hash "BCrypt"
        NUMBER tentativas_falhas
        TIMESTAMP bloqueado_ate
        NUMBER1 troca_senha_obrigatoria
    }
    SESSAO {
        RAW16 id_sessao PK
        RAW16 id_conta FK
        VARCHAR2 token_hash "SHA-256 do JWT"
        TIMESTAMP data_expiracao
        TIMESTAMP data_revogacao
        VARCHAR2 ip
        VARCHAR2 user_agent
    }
    PERFIL {
        RAW16 id_perfil PK
        VARCHAR2 nome UK
        VARCHAR2 descricao
        NUMBER1 ativo
    }
    PERMISSAO {
        RAW16 id_permissao PK
        VARCHAR2 codigo UK
        VARCHAR2 descricao
        NUMBER1 ativo
    }
    CONTA_PERFIL {
        RAW16 id_conta_perfil PK
        RAW16 id_conta FK
        RAW16 id_perfil FK
        NUMBER1 ativo
    }
    PERFIL_PERMISSAO {
        RAW16 id_perfil_permissao PK
        RAW16 id_perfil FK
        RAW16 id_permissao FK
    }
    TUTOR {
        RAW16 id_tutor PK
        RAW16 id_conta FK,UK
        VARCHAR2 nome
        DATE data_nascimento
    }
    VETERINARIO {
        RAW16 id_veterinario PK
        RAW16 id_conta FK,UK
        VARCHAR2 nome
        VARCHAR2 especialidade
        VARCHAR2 situacao_profissional
    }
    REGISTRO_VETERINARIO {
        RAW16 id_registro PK
        RAW16 id_veterinario FK
        VARCHAR2 numero_crmv "UK com uf"
        VARCHAR2 uf
        VARCHAR2 situacao
    }
    CLINICA {
        RAW16 id_clinica PK
        RAW16 id_conta FK,UK
        VARCHAR2 razao_social
        VARCHAR2 nome_fantasia
        VARCHAR2 endereco
        NUMBER1 ativa
    }
    ADMIN_CLINICA {
        RAW16 id_admin PK
        RAW16 id_conta FK,UK
        RAW16 id_clinica FK
        VARCHAR2 nome
    }
    VETERINARIO_CLINICA {
        RAW16 id_veterinario_clinica PK
        RAW16 id_veterinario FK
        RAW16 id_clinica FK
        DATE data_inicio
        DATE data_fim
        VARCHAR2 status_vinculo "ATIVO, INATIVO"
    }
    ESPECIE {
        RAW16 id_especie PK
        VARCHAR2 nome UK
    }
    RACA {
        RAW16 id_raca PK
        RAW16 id_especie FK
        VARCHAR2 nome
    }
    PET {
        RAW16 id_pet PK
        RAW16 id_tutor FK
        RAW16 id_raca FK
        VARCHAR2 nome
        DATE data_nascimento
        VARCHAR2 sexo
        NUMBER1 ativo
    }
    CONSULTA {
        RAW16 id_consulta PK
        RAW16 id_pet FK
        RAW16 id_veterinario FK
        RAW16 id_clinica FK
        TIMESTAMP data_hora
        VARCHAR2 modalidade "PRESENCIAL, TELEMEDICINA"
        VARCHAR2 status "AGENDADO, CONCLUIDA, CANCELADA"
    }
    PRONTUARIO {
        RAW16 id_prontuario PK
        RAW16 id_consulta FK,UK
        RAW16 id_veterinario FK
        VARCHAR2 notas_clinicas
        TIMESTAMP data_registro
    }
    AUTORIZACAO_ACESSO_PET {
        RAW16 id_autorizacao PK
        RAW16 id_pet FK
        RAW16 id_veterinario FK
        RAW16 id_clinica FK
        RAW16 id_conta_autorizador FK
        TIMESTAMP data_autorizacao
        TIMESTAMP data_expiracao
        VARCHAR2 status "ATIVA, REVOGADA"
        VARCHAR2 motivo_revogacao
    }
    NOTIFICACAO {
        RAW16 id_notificacao PK
        RAW16 id_conta FK
        VARCHAR2 mensagem
        TIMESTAMP data_criacao
        NUMBER1 lida
    }
```

## Decisões de modelagem

| Decisão | Por quê |
|---|---|
| **`conta_acesso` separada dos perfis** (`tutor`, `veterinario`, `clinica`) | Login, credencial e sessão são iguais para todos; os dados de cada ator ficam na própria tabela (3FN, sem colunas nulas por tipo). |
| **`credencial` e `identificador_acesso` fora da conta** | Senha e documentos (CPF/CNPJ) são dados sensíveis: ficam isolados, com hash, e podem mudar sem tocar na conta. |
| **`veterinario_clinica` (N:N com histórico)** | Um veterinário atende em várias clínicas; `data_inicio`/`data_fim` e `status_vinculo` guardam o histórico em vez de apagar o vínculo. |
| **`especie` → `raca` → `pet`** | Catálogo normalizado (V2) em vez de texto livre, evitando "Labrador" e "labrador" como valores diferentes. |
| **`prontuario` separado de `consulta` (1:0..1)** | A anotação clínica tem autor e data próprios; a consulta pode existir sem prontuário (agendada ou cancelada). |
| **`autorizacao_acesso_pet`** | Materializa o consentimento LGPD: quem autorizou, para qual profissional ou clínica, quando e o motivo de uma revogação. |
| **`sessao` com `token_hash`** | Permite revogar um JWT antes da expiração (logout), sem guardar o token em texto puro. |

## Padrões Oracle

* Chaves primárias `RAW(16) DEFAULT SYS_GUID()` (UUID).
* Booleanos como `NUMBER(1)` (`0`/`1`).
* Textos longos em `VARCHAR2(4000)`.
* Constraints nomeadas (`fk_*`, `uk_*`) para facilitar a leitura dos erros.

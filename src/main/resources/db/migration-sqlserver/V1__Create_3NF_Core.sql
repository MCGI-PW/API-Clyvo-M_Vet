-- =============================================================================
-- V1: Criação da Estrutura Base 3NF (T-SQL - Azure SQL Database / SQL Server)
-- Conversão de db/migration/V1 (Oracle):
--   RAW(16) DEFAULT SYS_GUID() -> UNIQUEIDENTIFIER DEFAULT NEWID()
--   VARCHAR2(n)                -> NVARCHAR(n)
--   NUMBER(1) (booleano)       -> BIT
--   NUMBER(10)                 -> INT
--   TIMESTAMP                  -> DATETIME2 (no SQL Server TIMESTAMP é rowversion)
-- =============================================================================

-- 1. CONTA_ACESSO
CREATE TABLE conta_acesso (
    id_conta UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    tipo_conta NVARCHAR(50) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    telefone NVARCHAR(50),
    status_conta NVARCHAR(50) NOT NULL,
    data_criacao DATETIME2 DEFAULT SYSDATETIME(),
    data_atualizacao DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT pk_conta_acesso PRIMARY KEY (id_conta),
    CONSTRAINT uk_conta_email UNIQUE (email)
);

-- 2. IDENTIFICADOR_ACESSO
CREATE TABLE identificador_acesso (
    id_identificador UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    tipo_identificador NVARCHAR(50) NOT NULL,
    valor_hash NVARCHAR(255) NOT NULL,
    valor_criptografado NVARCHAR(4000),
    ativo BIT DEFAULT 1,
    CONSTRAINT pk_identificador_acesso PRIMARY KEY (id_identificador),
    CONSTRAINT fk_identificador_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_tipo_valor UNIQUE (tipo_identificador, valor_hash)
);

-- 3. CREDENCIAL
CREATE TABLE credencial (
    id_credencial UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    senha_hash NVARCHAR(255) NOT NULL,
    data_ultima_alteracao DATETIME2 DEFAULT SYSDATETIME(),
    tentativas_falhas INT DEFAULT 0,
    bloqueado_ate DATETIME2,
    troca_senha_obrigatoria BIT DEFAULT 0,
    CONSTRAINT pk_credencial PRIMARY KEY (id_credencial),
    CONSTRAINT fk_credencial_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_credencial_conta UNIQUE (id_conta)
);

-- 4. TUTOR
CREATE TABLE tutor (
    id_tutor UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    nome NVARCHAR(255) NOT NULL,
    data_nascimento DATE,
    CONSTRAINT pk_tutor PRIMARY KEY (id_tutor),
    CONSTRAINT fk_tutor_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_tutor_conta UNIQUE (id_conta)
);

-- 5. PET
CREATE TABLE pet (
    id_pet UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_tutor UNIQUEIDENTIFIER NOT NULL,
    nome NVARCHAR(255) NOT NULL,
    data_nascimento DATE,
    sexo NVARCHAR(20),
    especie NVARCHAR(100),
    raca NVARCHAR(100),
    ativo BIT DEFAULT 1,
    CONSTRAINT pk_pet PRIMARY KEY (id_pet),
    CONSTRAINT fk_pet_tutor FOREIGN KEY (id_tutor) REFERENCES tutor(id_tutor)
);

-- 6. VETERINARIO
CREATE TABLE veterinario (
    id_veterinario UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    nome NVARCHAR(255) NOT NULL,
    especialidade NVARCHAR(255),
    situacao_profissional NVARCHAR(50),
    CONSTRAINT pk_veterinario PRIMARY KEY (id_veterinario),
    CONSTRAINT fk_veterinario_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_veterinario_conta UNIQUE (id_conta)
);

-- 7. REGISTRO_VETERINARIO
CREATE TABLE registro_veterinario (
    id_registro UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_veterinario UNIQUEIDENTIFIER NOT NULL,
    numero_crmv NVARCHAR(50) NOT NULL,
    uf NVARCHAR(2) NOT NULL,
    situacao NVARCHAR(50) NOT NULL,
    CONSTRAINT pk_registro_veterinario PRIMARY KEY (id_registro),
    CONSTRAINT fk_registro_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT uk_crmv_uf UNIQUE (numero_crmv, uf)
);

-- 8. CLINICA
CREATE TABLE clinica (
    id_clinica UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    razao_social NVARCHAR(255) NOT NULL,
    nome_fantasia NVARCHAR(255) NOT NULL,
    endereco NVARCHAR(4000),
    telefone NVARCHAR(50),
    ativa BIT DEFAULT 1,
    CONSTRAINT pk_clinica PRIMARY KEY (id_clinica),
    CONSTRAINT fk_clinica_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_clinica_conta UNIQUE (id_conta)
);

-- 9. ADMIN_CLINICA
CREATE TABLE admin_clinica (
    id_admin UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    id_clinica UNIQUEIDENTIFIER NOT NULL,
    nome NVARCHAR(255) NOT NULL,
    ativo BIT DEFAULT 1,
    CONSTRAINT pk_admin_clinica PRIMARY KEY (id_admin),
    CONSTRAINT fk_admin_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_admin_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica),
    CONSTRAINT uk_admin_conta UNIQUE (id_conta)
);

-- 10. VETERINARIO_CLINICA
CREATE TABLE veterinario_clinica (
    id_veterinario_clinica UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_veterinario UNIQUEIDENTIFIER NOT NULL,
    id_clinica UNIQUEIDENTIFIER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status_vinculo NVARCHAR(50) NOT NULL,
    CONSTRAINT pk_veterinario_clinica PRIMARY KEY (id_veterinario_clinica),
    CONSTRAINT fk_vc_veterinario FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_vc_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica),
    CONSTRAINT uk_vet_clinica UNIQUE (id_veterinario, id_clinica)
);

-- 11. PERFIL
CREATE TABLE perfil (
    id_perfil UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    nome NVARCHAR(100) NOT NULL,
    descricao NVARCHAR(255),
    ativo BIT DEFAULT 1,
    CONSTRAINT pk_perfil PRIMARY KEY (id_perfil),
    CONSTRAINT uk_perfil_nome UNIQUE (nome)
);

-- 12. PERMISSAO
CREATE TABLE permissao (
    id_permissao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    codigo NVARCHAR(100) NOT NULL,
    descricao NVARCHAR(255),
    ativo BIT DEFAULT 1,
    CONSTRAINT pk_permissao PRIMARY KEY (id_permissao),
    CONSTRAINT uk_permissao_codigo UNIQUE (codigo)
);

-- 13. CONTA_PERFIL
CREATE TABLE conta_perfil (
    id_conta_perfil UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    id_perfil UNIQUEIDENTIFIER NOT NULL,
    ativo BIT DEFAULT 1,
    data_atribuicao DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT pk_conta_perfil PRIMARY KEY (id_conta_perfil),
    CONSTRAINT fk_cp_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_cp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT uk_conta_perfil UNIQUE (id_conta, id_perfil)
);

-- 14. PERFIL_PERMISSAO
CREATE TABLE perfil_permissao (
    id_perfil_permissao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_perfil UNIQUEIDENTIFIER NOT NULL,
    id_permissao UNIQUEIDENTIFIER NOT NULL,
    CONSTRAINT pk_perfil_permissao PRIMARY KEY (id_perfil_permissao),
    CONSTRAINT fk_pp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT fk_pp_permissao FOREIGN KEY (id_permissao) REFERENCES permissao(id_permissao),
    CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao)
);

-- 15. AUTORIZACAO_ACESSO_PET
CREATE TABLE autorizacao_acesso_pet (
    id_autorizacao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_pet UNIQUEIDENTIFIER NOT NULL,
    id_veterinario UNIQUEIDENTIFIER NOT NULL,
    id_conta_autorizador UNIQUEIDENTIFIER NOT NULL,
    data_autorizacao DATETIME2 DEFAULT SYSDATETIME(),
    data_expiracao DATETIME2,
    status NVARCHAR(50) NOT NULL,
    motivo_revogacao NVARCHAR(4000),
    CONSTRAINT pk_autorizacao_acesso_pet PRIMARY KEY (id_autorizacao),
    CONSTRAINT fk_auth_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_auth_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_auth_conta FOREIGN KEY (id_conta_autorizador) REFERENCES conta_acesso(id_conta)
);

-- 16. SESSAO
CREATE TABLE sessao (
    id_sessao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_conta UNIQUEIDENTIFIER NOT NULL,
    token_hash NVARCHAR(255) NOT NULL,
    data_criacao DATETIME2 DEFAULT SYSDATETIME(),
    data_expiracao DATETIME2 NOT NULL,
    data_revogacao DATETIME2,
    ip NVARCHAR(50),
    user_agent NVARCHAR(4000),
    CONSTRAINT pk_sessao PRIMARY KEY (id_sessao),
    CONSTRAINT fk_sessao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);
GO

-- Inserção de Perfis Básicos
INSERT INTO perfil (nome, descricao) VALUES (N'TUTOR', N'Perfil de Tutor de Pet');
INSERT INTO perfil (nome, descricao) VALUES (N'VETERINARIO', N'Perfil de Veterinário');
INSERT INTO perfil (nome, descricao) VALUES (N'ADMIN_CLINICA', N'Administrador de Clínica');
INSERT INTO perfil (nome, descricao) VALUES (N'CLINICA', N'Conta de Pessoa Jurídica (Clínica)');

-- Inserção de Permissões Básicas
INSERT INTO permissao (codigo, descricao) VALUES (N'PET_CADASTRAR', N'Cadastrar Pets');
INSERT INTO permissao (codigo, descricao) VALUES (N'PET_VISUALIZAR', N'Visualizar Pets');
INSERT INTO permissao (codigo, descricao) VALUES (N'PET_EDITAR', N'Editar Pets');
INSERT INTO permissao (codigo, descricao) VALUES (N'CONSULTA_CRIAR', N'Criar Consultas');
GO

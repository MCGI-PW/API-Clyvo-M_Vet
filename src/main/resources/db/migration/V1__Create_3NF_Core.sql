-- =============================================================================
-- V1: Criação da Estrutura Base 3NF (Compatível com Oracle Database e H2 Oracle Mode)
-- =============================================================================

-- 1. CONTA_ACESSO
CREATE TABLE conta_acesso (
    id_conta RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    tipo_conta VARCHAR2(50) NOT NULL,
    email VARCHAR2(255) NOT NULL,
    telefone VARCHAR2(50),
    status_conta VARCHAR2(50) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_conta_email UNIQUE (email)
);

-- 2. IDENTIFICADOR_ACESSO
CREATE TABLE identificador_acesso (
    id_identificador RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    tipo_identificador VARCHAR2(50) NOT NULL,
    valor_hash VARCHAR2(255) NOT NULL,
    valor_criptografado VARCHAR2(4000),
    ativo NUMBER(1) DEFAULT 1,
    CONSTRAINT fk_identificador_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_tipo_valor UNIQUE (tipo_identificador, valor_hash)
);

-- 3. CREDENCIAL
CREATE TABLE credencial (
    id_credencial RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    senha_hash VARCHAR2(255) NOT NULL,
    data_ultima_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tentativas_falhas NUMBER(10) DEFAULT 0,
    bloqueado_ate TIMESTAMP,
    troca_senha_obrigatoria NUMBER(1) DEFAULT 0,
    CONSTRAINT fk_credencial_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_credencial_conta UNIQUE (id_conta)
);

-- 4. TUTOR
CREATE TABLE tutor (
    id_tutor RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    nome VARCHAR2(255) NOT NULL,
    data_nascimento DATE,
    CONSTRAINT fk_tutor_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_tutor_conta UNIQUE (id_conta)
);

-- 5. PET
CREATE TABLE pet (
    id_pet RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_tutor RAW(16) NOT NULL,
    nome VARCHAR2(255) NOT NULL,
    data_nascimento DATE,
    sexo VARCHAR2(20),
    especie VARCHAR2(100),
    raca VARCHAR2(100),
    ativo NUMBER(1) DEFAULT 1,
    CONSTRAINT fk_pet_tutor FOREIGN KEY (id_tutor) REFERENCES tutor(id_tutor)
);

-- 6. VETERINARIO
CREATE TABLE veterinario (
    id_veterinario RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    nome VARCHAR2(255) NOT NULL,
    especialidade VARCHAR2(255),
    situacao_profissional VARCHAR2(50),
    CONSTRAINT fk_veterinario_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_veterinario_conta UNIQUE (id_conta)
);

-- 7. REGISTRO_VETERINARIO
CREATE TABLE registro_veterinario (
    id_registro RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_veterinario RAW(16) NOT NULL,
    numero_crmv VARCHAR2(50) NOT NULL,
    uf VARCHAR2(2) NOT NULL,
    situacao VARCHAR2(50) NOT NULL,
    CONSTRAINT fk_registro_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT uk_crmv_uf UNIQUE (numero_crmv, uf)
);

-- 8. CLINICA
CREATE TABLE clinica (
    id_clinica RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    razao_social VARCHAR2(255) NOT NULL,
    nome_fantasia VARCHAR2(255) NOT NULL,
    endereco VARCHAR2(4000),
    telefone VARCHAR2(50),
    ativa NUMBER(1) DEFAULT 1,
    CONSTRAINT fk_clinica_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_clinica_conta UNIQUE (id_conta)
);

-- 9. ADMIN_CLINICA
CREATE TABLE admin_clinica (
    id_admin RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    id_clinica RAW(16) NOT NULL,
    nome VARCHAR2(255) NOT NULL,
    ativo NUMBER(1) DEFAULT 1,
    CONSTRAINT fk_admin_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_admin_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica),
    CONSTRAINT uk_admin_conta UNIQUE (id_conta)
);

-- 10. VETERINARIO_CLINICA
CREATE TABLE veterinario_clinica (
    id_veterinario_clinica RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_veterinario RAW(16) NOT NULL,
    id_clinica RAW(16) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status_vinculo VARCHAR2(50) NOT NULL,
    CONSTRAINT fk_vc_veterinario FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_vc_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica),
    CONSTRAINT uk_vet_clinica UNIQUE (id_veterinario, id_clinica)
);

-- 11. PERFIL
CREATE TABLE perfil (
    id_perfil RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    nome VARCHAR2(100) NOT NULL,
    descricao VARCHAR2(255),
    ativo NUMBER(1) DEFAULT 1,
    CONSTRAINT uk_perfil_nome UNIQUE (nome)
);

-- 12. PERMISSAO
CREATE TABLE permissao (
    id_permissao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    codigo VARCHAR2(100) NOT NULL,
    descricao VARCHAR2(255),
    ativo NUMBER(1) DEFAULT 1,
    CONSTRAINT uk_permissao_codigo UNIQUE (codigo)
);

-- 13. CONTA_PERFIL
CREATE TABLE conta_perfil (
    id_conta_perfil RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    id_perfil RAW(16) NOT NULL,
    ativo NUMBER(1) DEFAULT 1,
    data_atribuicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cp_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_cp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT uk_conta_perfil UNIQUE (id_conta, id_perfil)
);

-- 14. PERFIL_PERMISSAO
CREATE TABLE perfil_permissao (
    id_perfil_permissao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_perfil RAW(16) NOT NULL,
    id_permissao RAW(16) NOT NULL,
    CONSTRAINT fk_pp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT fk_pp_permissao FOREIGN KEY (id_permissao) REFERENCES permissao(id_permissao),
    CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao)
);

-- 15. AUTORIZACAO_ACESSO_PET
CREATE TABLE autorizacao_acesso_pet (
    id_autorizacao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_pet RAW(16) NOT NULL,
    id_veterinario RAW(16) NOT NULL,
    id_conta_autorizador RAW(16) NOT NULL,
    data_autorizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    status VARCHAR2(50) NOT NULL,
    motivo_revogacao VARCHAR2(4000),
    CONSTRAINT fk_auth_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_auth_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_auth_conta FOREIGN KEY (id_conta_autorizador) REFERENCES conta_acesso(id_conta)
);

-- 16. SESSAO
CREATE TABLE sessao (
    id_sessao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_conta RAW(16) NOT NULL,
    token_hash VARCHAR2(255) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NOT NULL,
    data_revogacao TIMESTAMP,
    ip VARCHAR2(50),
    user_agent VARCHAR2(4000),
    CONSTRAINT fk_sessao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- Inserção de Perfis Básicos
INSERT INTO perfil (nome, descricao) VALUES ('TUTOR', 'Perfil de Tutor de Pet');
INSERT INTO perfil (nome, descricao) VALUES ('VETERINARIO', 'Perfil de Veterinário');
INSERT INTO perfil (nome, descricao) VALUES ('ADMIN_CLINICA', 'Administrador de Clínica');
INSERT INTO perfil (nome, descricao) VALUES ('CLINICA', 'Conta de Pessoa Jurídica (Clínica)');

-- Inserção de Permissões Básicas
INSERT INTO permissao (codigo, descricao) VALUES ('PET_CADASTRAR', 'Cadastrar Pets');
INSERT INTO permissao (codigo, descricao) VALUES ('PET_VISUALIZAR', 'Visualizar Pets');
INSERT INTO permissao (codigo, descricao) VALUES ('PET_EDITAR', 'Editar Pets');
INSERT INTO permissao (codigo, descricao) VALUES ('CONSULTA_CRIAR', 'Criar Consultas');

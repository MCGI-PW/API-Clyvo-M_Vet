CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. CONTA_ACESSO
CREATE TABLE conta_acesso (
    id_conta UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_conta VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(50),
    status_conta VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. IDENTIFICADOR_ACESSO
CREATE TABLE identificador_acesso (
    id_identificador UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL,
    tipo_identificador VARCHAR(50) NOT NULL,
    valor_hash VARCHAR(255) NOT NULL,
    valor_criptografado TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_identificador_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT uk_tipo_valor UNIQUE (tipo_identificador, valor_hash)
);

-- 3. CREDENCIAL
CREATE TABLE credencial (
    id_credencial UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_ultima_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tentativas_falhas INT DEFAULT 0,
    bloqueado_ate TIMESTAMP,
    troca_senha_obrigatoria BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_credencial_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- 4. TUTOR
CREATE TABLE tutor (
    id_tutor UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    CONSTRAINT fk_tutor_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- 5. PET
CREATE TABLE pet (
    id_pet UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_tutor UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    sexo VARCHAR(20),
    especie VARCHAR(100),
    raca VARCHAR(100),
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_pet_tutor FOREIGN KEY (id_tutor) REFERENCES tutor(id_tutor)
);

-- 6. VETERINARIO
CREATE TABLE veterinario (
    id_veterinario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    especialidade VARCHAR(255),
    situacao_profissional VARCHAR(50),
    CONSTRAINT fk_veterinario_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- 7. REGISTRO_VETERINARIO
CREATE TABLE registro_veterinario (
    id_registro UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_veterinario UUID NOT NULL,
    numero_crmv VARCHAR(50) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    situacao VARCHAR(50) NOT NULL,
    CONSTRAINT fk_registro_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT uk_crmv_uf UNIQUE (numero_crmv, uf)
);

-- 8. CLINICA
CREATE TABLE clinica (
    id_clinica UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL UNIQUE,
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255) NOT NULL,
    endereco TEXT,
    telefone VARCHAR(50),
    ativa BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_clinica_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- 9. ADMIN_CLINICA
CREATE TABLE admin_clinica (
    id_admin UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL UNIQUE,
    id_clinica UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_admin_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_admin_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica)
);

-- 10. VETERINARIO_CLINICA
CREATE TABLE veterinario_clinica (
    id_veterinario_clinica UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_veterinario UUID NOT NULL,
    id_clinica UUID NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    status_vinculo VARCHAR(50) NOT NULL,
    CONSTRAINT fk_vc_veterinario FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_vc_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica),
    CONSTRAINT uk_vet_clinica UNIQUE (id_veterinario, id_clinica)
);

-- 11. PERFIL
CREATE TABLE perfil (
    id_perfil UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE
);

-- 12. PERMISSAO
CREATE TABLE permissao (
    id_permissao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ativo BOOLEAN DEFAULT TRUE
);

-- 13. CONTA_PERFIL
CREATE TABLE conta_perfil (
    id_conta_perfil UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL,
    id_perfil UUID NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_atribuicao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cp_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta),
    CONSTRAINT fk_cp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT uk_conta_perfil UNIQUE (id_conta, id_perfil)
);

-- 14. PERFIL_PERMISSAO
CREATE TABLE perfil_permissao (
    id_perfil_permissao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_perfil UUID NOT NULL,
    id_permissao UUID NOT NULL,
    CONSTRAINT fk_pp_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil),
    CONSTRAINT fk_pp_permissao FOREIGN KEY (id_permissao) REFERENCES permissao(id_permissao),
    CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao)
);

-- 15. AUTORIZACAO_ACESSO_PET
CREATE TABLE autorizacao_acesso_pet (
    id_autorizacao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pet UUID NOT NULL,
    id_veterinario UUID NOT NULL,
    id_conta_autorizador UUID NOT NULL,
    data_autorizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    motivo_revogacao TEXT,
    CONSTRAINT fk_auth_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_auth_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario),
    CONSTRAINT fk_auth_conta FOREIGN KEY (id_conta_autorizador) REFERENCES conta_acesso(id_conta)
);

-- 16. SESSAO
CREATE TABLE sessao (
    id_sessao UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_conta UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NOT NULL,
    data_revogacao TIMESTAMP,
    ip VARCHAR(50),
    user_agent TEXT,
    CONSTRAINT fk_sessao_conta FOREIGN KEY (id_conta) REFERENCES conta_acesso(id_conta)
);

-- Inserindo Perfis Basicos
INSERT INTO perfil (nome, descricao) VALUES 
('TUTOR', 'Perfil de Tutor de Pet'),
('VETERINARIO', 'Perfil de Veterinário'),
('ADMIN_CLINICA', 'Administrador de Clínica'),
('CLINICA', 'Conta de Pessoa Jurídica (Clínica)');

-- Inserindo Permissoes Basicas
INSERT INTO permissao (codigo, descricao) VALUES
('PET_CADASTRAR', 'Cadastrar Pets'),
('PET_VISUALIZAR', 'Visualizar Pets'),
('PET_EDITAR', 'Editar Pets'),
('CONSULTA_CRIAR', 'Criar Consultas');

-- (Opcional) Associando permissoes... deixaremos o Java fazer ou script separado depois

-- Adiciona id_clinica em consulta
ALTER TABLE consulta ADD COLUMN IF NOT EXISTS id_clinica UUID;
ALTER TABLE consulta ADD CONSTRAINT fk_consulta_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);

-- Adiciona id_clinica em autorizacao_acesso_pet
ALTER TABLE autorizacao_acesso_pet ADD COLUMN IF NOT EXISTS id_clinica UUID;
ALTER TABLE autorizacao_acesso_pet ADD CONSTRAINT fk_auth_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);

-- Popula clinicas e vinculos
DO $$ 
DECLARE
    id_conta_c1 UUID := gen_random_uuid();
    id_conta_c2 UUID := gen_random_uuid();
    id_clinica1 UUID := gen_random_uuid();
    id_clinica2 UUID := gen_random_uuid();
    v_roberto UUID;
    v_camila UUID;
    v_marcos UUID;
BEGIN
    -- Conta e Credencial Clinica 1 (Central)
    INSERT INTO conta_acesso (id_conta, email, telefone, tipo_conta, status_conta, data_criacao, data_atualizacao)
    VALUES (id_conta_c1, 'clinica.central@clyvo.com', '1133334441', 'CLINICA', 'ATIVO', current_timestamp, current_timestamp)
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO credencial (id_conta, senha_hash, tentativas_falhas, troca_senha_obrigatoria, data_ultima_alteracao)
    VALUES (id_conta_c1, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0, false, current_timestamp)
    ON CONFLICT (id_conta) DO NOTHING;

    INSERT INTO clinica (id_clinica, id_conta, razao_social, nome_fantasia, endereco, telefone, ativa)
    VALUES (id_clinica1, id_conta_c1, 'Hospital Veterinário Clyvo Central Ltda', 'Clyvo Central', 'Av. Paulista, 1000 - Bela Vista, SP', '1133334441', true);

    -- Conta e Credencial Clinica 2 (Jardins)
    INSERT INTO conta_acesso (id_conta, email, telefone, tipo_conta, status_conta, data_criacao, data_atualizacao)
    VALUES (id_conta_c2, 'clinica.jardins@clyvo.com', '1133334442', 'CLINICA', 'ATIVO', current_timestamp, current_timestamp)
    ON CONFLICT (email) DO NOTHING;

    INSERT INTO credencial (id_conta, senha_hash, tentativas_falhas, troca_senha_obrigatoria, data_ultima_alteracao)
    VALUES (id_conta_c2, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0, false, current_timestamp)
    ON CONFLICT (id_conta) DO NOTHING;

    INSERT INTO clinica (id_clinica, id_conta, razao_social, nome_fantasia, endereco, telefone, ativa)
    VALUES (id_clinica2, id_conta_c2, 'Clínica Veterinária Clyvo Jardins Eireli', 'Clyvo Jardins', 'Rua Oscar Freire, 500 - Jardins, SP', '1133334442', true);

    -- Buscar os IDs dos veterinários criados na V3
    SELECT id_veterinario INTO v_roberto FROM veterinario WHERE nome LIKE '%Roberto%' LIMIT 1;
    SELECT id_veterinario INTO v_camila FROM veterinario WHERE nome LIKE '%Camila%' LIMIT 1;
    SELECT id_veterinario INTO v_marcos FROM veterinario WHERE nome LIKE '%Marcos%' LIMIT 1;

    -- Vincular veterinarios as clinicas
    -- Dr. Roberto atua na Clinica 1 e na Clinica 2 (multi-clinica)
    IF v_roberto IS NOT NULL THEN
        INSERT INTO veterinario_clinica (id_veterinario, id_clinica, data_inicio, status_vinculo)
        VALUES (v_roberto, id_clinica1, current_date, 'ATIVO')
        ON CONFLICT (id_veterinario, id_clinica) DO NOTHING;

        INSERT INTO veterinario_clinica (id_veterinario, id_clinica, data_inicio, status_vinculo)
        VALUES (v_roberto, id_clinica2, current_date, 'ATIVO')
        ON CONFLICT (id_veterinario, id_clinica) DO NOTHING;
    END IF;

    -- Dra. Camila atua na Clinica 1
    IF v_camila IS NOT NULL THEN
        INSERT INTO veterinario_clinica (id_veterinario, id_clinica, data_inicio, status_vinculo)
        VALUES (v_camila, id_clinica1, current_date, 'ATIVO')
        ON CONFLICT (id_veterinario, id_clinica) DO NOTHING;
    END IF;

    -- Dr. Marcos atua na Clinica 2
    IF v_marcos IS NOT NULL THEN
        INSERT INTO veterinario_clinica (id_veterinario, id_clinica, data_inicio, status_vinculo)
        VALUES (v_marcos, id_clinica2, current_date, 'ATIVO')
        ON CONFLICT (id_veterinario, id_clinica) DO NOTHING;
    END IF;

    -- Atualiza consultas pré-existentes sem clinica para a Clinica 1
    UPDATE consulta SET id_clinica = id_clinica1 WHERE id_clinica IS NULL;
END $$;

CREATE TABLE consulta (
    id_consulta UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pet UUID NOT NULL,
    id_veterinario UUID NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    modalidade VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'AGENDADO',
    CONSTRAINT fk_consulta_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_consulta_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario)
);

-- Populating dummy veterinarians
DO $$ 
DECLARE
    id_conta1 UUID := gen_random_uuid();
    id_conta2 UUID := gen_random_uuid();
    id_conta3 UUID := gen_random_uuid();
BEGIN
    INSERT INTO conta_acesso (id_conta, email, telefone, tipo_conta, status_conta, data_criacao, data_atualizacao)
    VALUES 
    (id_conta1, 'vet1@clyvo.com', '11999999991', 'VETERINARIO', 'ATIVO', current_timestamp, current_timestamp),
    (id_conta2, 'vet2@clyvo.com', '11999999992', 'VETERINARIO', 'ATIVO', current_timestamp, current_timestamp),
    (id_conta3, 'vet3@clyvo.com', '11999999993', 'VETERINARIO', 'ATIVO', current_timestamp, current_timestamp);

    INSERT INTO credencial (id_conta, senha_hash, tentativas_falhas, troca_senha_obrigatoria, data_ultima_alteracao)
    VALUES 
    (id_conta1, 'dummyhash1', 0, false, current_timestamp),
    (id_conta2, 'dummyhash2', 0, false, current_timestamp),
    (id_conta3, 'dummyhash3', 0, false, current_timestamp);

    INSERT INTO veterinario (id_conta, nome, especialidade, situacao_profissional)
    VALUES 
    (id_conta1, 'Dr. Roberto Silveira', 'Clínico Geral', 'REGULAR'),
    (id_conta2, 'Dra. Camila Nogueira', 'Dermatologia', 'REGULAR'),
    (id_conta3, 'Dr. Marcos Santos', 'Ortopedia', 'REGULAR');
END $$;

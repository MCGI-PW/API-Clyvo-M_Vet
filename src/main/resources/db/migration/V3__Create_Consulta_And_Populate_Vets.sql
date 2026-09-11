-- =============================================================================
-- V3: Criação da Tabela Consulta (Compatível com Oracle e H2)
-- =============================================================================

CREATE TABLE consulta (
    id_consulta RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_pet RAW(16) NOT NULL,
    id_veterinario RAW(16) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    modalidade VARCHAR2(50) NOT NULL,
    status VARCHAR2(50) DEFAULT 'AGENDADO' NOT NULL,
    CONSTRAINT fk_consulta_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_consulta_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario)
);

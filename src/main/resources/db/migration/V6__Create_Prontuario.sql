-- =============================================================================
-- V6: Criação da Tabela Prontuário Clínico (Compatível com Oracle e H2)
-- =============================================================================

CREATE TABLE prontuario (
    id_prontuario RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_consulta RAW(16) NOT NULL,
    id_veterinario RAW(16) NOT NULL,
    notas_clinicas VARCHAR2(4000) NOT NULL,
    data_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_prontuario_consulta UNIQUE (id_consulta),
    CONSTRAINT fk_prontuario_consulta FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta),
    CONSTRAINT fk_prontuario_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario)
);

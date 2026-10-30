-- =============================================================================
-- V7: Transcrição da teleconsulta e resumo clínico gerado por IA (Oracle e H2)
-- A transcrição é texto livre e pode passar de 4000 caracteres, por isso CLOB.
-- =============================================================================

CREATE TABLE transcricao_consulta (
    id_transcricao RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_consulta RAW(16) NOT NULL,
    texto_transcricao CLOB NOT NULL,
    diagnostico_sugerido VARCHAR2(4000),
    prescricao_sugerida VARCHAR2(4000),
    orientacoes_sugeridas VARCHAR2(4000),
    resumo_tutor VARCHAR2(4000),
    sinais_alerta VARCHAR2(4000),
    fonte_ia VARCHAR2(30) NOT NULL,
    data_geracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_transcricao_consulta FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)
);

CREATE INDEX idx_transcricao_consulta ON transcricao_consulta (id_consulta);

-- =============================================================================
-- V7: Transcrição da teleconsulta e resumo clínico gerado por IA (T-SQL - Azure SQL)
-- A transcrição é texto livre e pode passar de 4000 caracteres:
-- CLOB do Oracle -> NVARCHAR(MAX) no SQL Server (até 2 GB).
-- =============================================================================

CREATE TABLE transcricao_consulta (
    id_transcricao UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_consulta UNIQUEIDENTIFIER NOT NULL,
    texto_transcricao NVARCHAR(MAX) NOT NULL,
    diagnostico_sugerido NVARCHAR(4000),
    prescricao_sugerida NVARCHAR(4000),
    orientacoes_sugeridas NVARCHAR(4000),
    resumo_tutor NVARCHAR(4000),
    sinais_alerta NVARCHAR(4000),
    fonte_ia NVARCHAR(30) NOT NULL,
    data_geracao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_transcricao_consulta PRIMARY KEY (id_transcricao),
    CONSTRAINT fk_transcricao_consulta FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta)
);

CREATE INDEX idx_transcricao_consulta ON transcricao_consulta (id_consulta);
GO

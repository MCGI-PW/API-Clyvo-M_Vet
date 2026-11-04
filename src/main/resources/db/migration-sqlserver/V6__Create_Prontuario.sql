-- =============================================================================
-- V6: Criação da Tabela Prontuário Clínico (T-SQL - Azure SQL / SQL Server)
-- =============================================================================

CREATE TABLE prontuario (
    id_prontuario UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_consulta UNIQUEIDENTIFIER NOT NULL,
    id_veterinario UNIQUEIDENTIFIER NOT NULL,
    notas_clinicas NVARCHAR(4000) NOT NULL,
    data_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT pk_prontuario PRIMARY KEY (id_prontuario),
    CONSTRAINT uk_prontuario_consulta UNIQUE (id_consulta),
    CONSTRAINT fk_prontuario_consulta FOREIGN KEY (id_consulta) REFERENCES consulta(id_consulta),
    CONSTRAINT fk_prontuario_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario)
);
GO

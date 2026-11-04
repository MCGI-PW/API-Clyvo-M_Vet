-- =============================================================================
-- V3: Criação da Tabela Consulta (T-SQL - Azure SQL / SQL Server)
-- =============================================================================

CREATE TABLE consulta (
    id_consulta UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_pet UNIQUEIDENTIFIER NOT NULL,
    id_veterinario UNIQUEIDENTIFIER NOT NULL,
    data_hora DATETIME2 NOT NULL,
    modalidade NVARCHAR(50) NOT NULL,
    status NVARCHAR(50) NOT NULL DEFAULT N'AGENDADO',
    CONSTRAINT pk_consulta PRIMARY KEY (id_consulta),
    CONSTRAINT fk_consulta_pet FOREIGN KEY (id_pet) REFERENCES pet(id_pet),
    CONSTRAINT fk_consulta_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario)
);
GO

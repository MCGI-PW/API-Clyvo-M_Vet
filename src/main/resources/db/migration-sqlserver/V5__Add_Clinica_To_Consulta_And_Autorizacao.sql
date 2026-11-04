-- =============================================================================
-- V5: Adicionar Clínica a Consulta e Autorização (T-SQL - Azure SQL / SQL Server)
-- O "GO" garante que a coluna nova exista antes da FK que a referencia.
-- =============================================================================

ALTER TABLE consulta ADD id_clinica UNIQUEIDENTIFIER NULL;
ALTER TABLE autorizacao_acesso_pet ADD id_clinica UNIQUEIDENTIFIER NULL;
GO

ALTER TABLE consulta ADD CONSTRAINT fk_consulta_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);
ALTER TABLE autorizacao_acesso_pet ADD CONSTRAINT fk_auth_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);
GO

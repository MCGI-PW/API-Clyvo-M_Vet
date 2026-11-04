-- =============================================================================
-- V8: A plataforma passou a se chamar Aurora Pet (T-SQL - Azure SQL / SQL Server)
-- Atualiza o nome das clínicas já gravadas, como a de demonstração "Clínica Clyvo Central".
-- Num banco novo não há linhas ainda, e o DataInitializer já cria com o nome novo.
-- =============================================================================

UPDATE clinica
   SET razao_social  = REPLACE(razao_social, N'Clyvo', N'Aurora'),
       nome_fantasia = REPLACE(nome_fantasia, N'Clyvo', N'Aurora')
 WHERE razao_social LIKE N'%Clyvo%' OR nome_fantasia LIKE N'%Clyvo%';
GO

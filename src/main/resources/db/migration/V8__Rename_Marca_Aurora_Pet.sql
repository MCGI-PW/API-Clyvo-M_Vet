-- =============================================================================
-- V8: A plataforma passou a se chamar Aurora Pet (Oracle e H2)
-- Atualiza o nome das clínicas já gravadas, como a de demonstração "Clínica Clyvo Central".
-- Num banco novo não há linhas ainda, e o DataInitializer já cria com o nome novo.
-- =============================================================================

UPDATE clinica
   SET razao_social  = REPLACE(razao_social, 'Clyvo', 'Aurora'),
       nome_fantasia = REPLACE(nome_fantasia, 'Clyvo', 'Aurora')
 WHERE razao_social LIKE '%Clyvo%' OR nome_fantasia LIKE '%Clyvo%';

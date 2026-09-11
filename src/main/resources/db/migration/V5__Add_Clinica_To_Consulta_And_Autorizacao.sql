-- =============================================================================
-- V5: Adicionar Clínica a Consulta e Autorização (Compatível com Oracle e H2)
-- =============================================================================

ALTER TABLE consulta ADD (id_clinica RAW(16));
ALTER TABLE consulta ADD CONSTRAINT fk_consulta_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);

ALTER TABLE autorizacao_acesso_pet ADD (id_clinica RAW(16));
ALTER TABLE autorizacao_acesso_pet ADD CONSTRAINT fk_auth_clinica FOREIGN KEY (id_clinica) REFERENCES clinica(id_clinica);

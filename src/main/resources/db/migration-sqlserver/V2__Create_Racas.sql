-- =============================================================================
-- V2: Criação de Espécies, Raças e Normalização de Pet (T-SQL - Azure SQL / SQL Server)
-- Os "GO" separam os lotes: o SQL Server precisa que a coluna nova exista
-- antes de outro comando usá-la no mesmo script.
-- =============================================================================

CREATE TABLE especie (
    id_especie UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    nome NVARCHAR(100) NOT NULL,
    CONSTRAINT pk_especie PRIMARY KEY (id_especie),
    CONSTRAINT uk_especie_nome UNIQUE (nome)
);

CREATE TABLE raca (
    id_raca UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    id_especie UNIQUEIDENTIFIER NOT NULL,
    nome NVARCHAR(255) NOT NULL,
    CONSTRAINT pk_raca PRIMARY KEY (id_raca),
    CONSTRAINT fk_raca_especie FOREIGN KEY (id_especie) REFERENCES especie(id_especie)
);
GO

-- Modificando a tabela PET
ALTER TABLE pet ADD id_raca UNIQUEIDENTIFIER NULL;
GO
ALTER TABLE pet ADD CONSTRAINT fk_pet_raca FOREIGN KEY (id_raca) REFERENCES raca(id_raca);
ALTER TABLE pet DROP COLUMN raca;
ALTER TABLE pet DROP COLUMN especie;
GO

-- Inserindo Espécies
INSERT INTO especie (nome) VALUES (N'Cachorro');
INSERT INTO especie (nome) VALUES (N'Gato');
INSERT INTO especie (nome) VALUES (N'Ave');
INSERT INTO especie (nome) VALUES (N'Reptil');
INSERT INTO especie (nome) VALUES (N'Pequeno Mamifero');

-- Inserindo Raças de Cachorro
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Labrador Retriever' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Golden Retriever' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Bulldog Frances' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Bulldog Ingles' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Pastor Alemao' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Poodle' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Beagle' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Rottweiler' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Pinscher' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Shih Tzu' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Pug' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Spitz Alemao' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Yorkshire Terrier' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Border Collie' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Dachshund' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Husky Siberiano' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Chihuahua' FROM especie WHERE nome = N'Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'SRD (Sem Raca Definida - Vira-lata)' FROM especie WHERE nome = N'Cachorro';

-- Inserindo Raças de Gato
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Persa' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Maine Coon' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Siames' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Ragdoll' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Sphynx' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Bengal' FROM especie WHERE nome = N'Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'SRD (Sem Raca Definida)' FROM especie WHERE nome = N'Gato';

-- Inserindo Raças de Aves
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Calopsita' FROM especie WHERE nome = N'Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Canario' FROM especie WHERE nome = N'Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, N'Papagaio Verdadeiro' FROM especie WHERE nome = N'Ave';
GO

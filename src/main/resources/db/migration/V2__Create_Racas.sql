-- =============================================================================
-- V2: Criação de Espécies, Raças e Normalização de Pet (Compatível com Oracle e H2)
-- =============================================================================

CREATE TABLE especie (
    id_especie RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    nome VARCHAR2(100) NOT NULL,
    CONSTRAINT uk_especie_nome UNIQUE (nome)
);

CREATE TABLE raca (
    id_raca RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    id_especie RAW(16) NOT NULL,
    nome VARCHAR2(255) NOT NULL,
    CONSTRAINT fk_raca_especie FOREIGN KEY (id_especie) REFERENCES especie(id_especie)
);

-- Modificando a tabela PET
ALTER TABLE pet ADD (id_raca RAW(16));
ALTER TABLE pet ADD CONSTRAINT fk_pet_raca FOREIGN KEY (id_raca) REFERENCES raca(id_raca);
ALTER TABLE pet DROP COLUMN raca;
ALTER TABLE pet DROP COLUMN especie;

-- Inserindo Espécies
INSERT INTO especie (nome) VALUES ('Cachorro');
INSERT INTO especie (nome) VALUES ('Gato');
INSERT INTO especie (nome) VALUES ('Ave');
INSERT INTO especie (nome) VALUES ('Reptil');
INSERT INTO especie (nome) VALUES ('Pequeno Mamifero');

-- Inserindo Raças de Cachorro
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Labrador Retriever' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Golden Retriever' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Bulldog Frances' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Bulldog Ingles' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Pastor Alemao' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Poodle' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Beagle' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Rottweiler' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Pinscher' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Shih Tzu' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Pug' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Spitz Alemao' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Yorkshire Terrier' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Border Collie' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Dachshund' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Husky Siberiano' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Chihuahua' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'SRD (Sem Raca Definida - Vira-lata)' FROM especie WHERE nome='Cachorro';

-- Inserindo Raças de Gato
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Persa' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Maine Coon' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Siames' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Ragdoll' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Sphynx' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Bengal' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'SRD (Sem Raca Definida)' FROM especie WHERE nome='Gato';

-- Inserindo Raças de Aves
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Calopsita' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Canario' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Papagaio Verdadeiro' FROM especie WHERE nome='Ave';

CREATE TABLE especie (
    id_especie UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE raca (
    id_raca UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_especie UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    CONSTRAINT fk_raca_especie FOREIGN KEY (id_especie) REFERENCES especie(id_especie)
);

-- Modificando a tabela PET
ALTER TABLE pet ADD COLUMN id_raca UUID;
ALTER TABLE pet ADD CONSTRAINT fk_pet_raca FOREIGN KEY (id_raca) REFERENCES raca(id_raca);
-- Removemos a coluna raca antiga em favor da nova normalizada
ALTER TABLE pet DROP COLUMN raca;
ALTER TABLE pet DROP COLUMN especie;

-- Inserindo Especies
INSERT INTO especie (nome) VALUES ('Cachorro'), ('Gato'), ('Ave'), ('Reptil'), ('Pequeno Mamifero'), ('Animal de Fazenda / Preservacao');

-- Inserindo Racas de Cachorro
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
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Spitz Alemao (Lulu da Pomerania)' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Yorkshire Terrier' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Border Collie' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Dachshund (Salsicha)' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Basset Hound' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Husky Siberiano' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Chihuahua' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'SRD (Sem Raca Definida - Vira-lata)' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Doberman' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Bichon Frise' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Maltes' FROM especie WHERE nome='Cachorro';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Pitbull' FROM especie WHERE nome='Cachorro';

-- Inserindo Racas de Gato
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Persa' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Maine Coon' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Siamês' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Ragdoll' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Sphynx' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Bengal' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Munchkin' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Russian Blue' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Scottish Fold' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'SRD (Sem Raca Definida)' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Ashera' FROM especie WHERE nome='Gato';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Savannah' FROM especie WHERE nome='Gato';

-- Inserindo Aves
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Calopsita' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Papagaio Verdadeiro' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Canario da Terra' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Periquito Australiano' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Arara Azul' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Cacatua' FROM especie WHERE nome='Ave';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Tucano' FROM especie WHERE nome='Ave';

-- Inserindo Repteis e Anfibios
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Iguana Verde' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Cobra do Milho (Corn Snake)' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Tartaruga Tigre D''Agua' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Jiboia' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Dragao Barbudo' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Gecko Leopardo' FROM especie WHERE nome='Reptil';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Teiu' FROM especie WHERE nome='Reptil';

-- Inserindo Pequenos Mamiferos
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Coelho Anao' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Coelho Lionhead' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Hamster Sirio' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Hamster Anao Russo' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Porquinho-da-Índia' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Chinchila' FROM especie WHERE nome='Pequeno Mamifero';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Ferret (Furao)' FROM especie WHERE nome='Pequeno Mamifero';

-- Inserindo Animais de Fazenda / Preservacao
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Cavalo Quarto de Milha' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Cavalo Mangalarga' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Ovelha' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Cabra' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Porco Mini Pig' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Lhama' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Alpaca' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Macaco-Prego' FROM especie WHERE nome='Animal de Fazenda / Preservacao';
INSERT INTO raca (id_especie, nome) SELECT id_especie, 'Sagui' FROM especie WHERE nome='Animal de Fazenda / Preservacao';

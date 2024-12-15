/*
DROP DATABASE comparateur_db;
CREATE DATABASE comparateur_db;
*/
use comparateur_db;


-- Vider les tables pour réinitialisation
-- Désactiver les contraintes de clé étrangère
SET foreign_key_checks = 0;
TRUNCATE TABLE hotel_agence;
TRUNCATE TABLE hotel;
TRUNCATE TABLE agence;
TRUNCATE TABLE adresse;
-- Réactiver les contraintes de clé étrangère
SET foreign_key_checks = 1;

-- Insertion des données dans la table adresse
INSERT INTO adresse (id, pays, ville, nom_rue, numero_rue) 
VALUES (1, 'France', 'Paris', 'Avenue George V', 101);

INSERT INTO adresse (id, pays, ville, nom_rue, numero_rue) 
VALUES (2, 'France', 'Marseille', 'Rue de la République', 50);


-- Insertion des données dans la table agence
INSERT INTO agence (id, nom, password) 
VALUES (1, 'Agence Parisienne', 'password123');

INSERT INTO agence (id, nom, password) 
VALUES (2, 'Agence Marseillaise', 'password456');

-- Insertion des données dans la table hotel
INSERT INTO hotel (id, nom, adresse_id, nb_etoiles, nb_lits_disponibles) 
VALUES (1, 'Hôtel de Paris', 1, 4, 50);

-- Insertion des données dans la table agence_hotel (relation plusieurs-à-plusieurs)
INSERT INTO hotel_agence (agence_id, hotel_id) 
VALUES (1, 1);

INSERT INTO hotel_agence (agence_id, hotel_id) 
VALUES (2, 1);


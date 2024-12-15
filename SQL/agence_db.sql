/*
DROP DATABASE agence_db;
CREATE DATABASE agence_db;

*/
USE agence_db;

-- nettoyage des tables 

-- Nettoyage des tables

-- Désactiver les contraintes de clé étrangère
SET foreign_key_checks = 0;

-- Nettoyage des tables
TRUNCATE TABLE agence_hotel;
TRUNCATE TABLE agence_client;
TRUNCATE TABLE carte_bancaire;
TRUNCATE TABLE client;
TRUNCATE TABLE hotel;
TRUNCATE TABLE agence;
TRUNCATE TABLE adresse;

-- Réactiver les contraintes de clé étrangère
SET foreign_key_checks = 1;



-- Insertion des données
INSERT INTO adresse (pays, ville, nom_rue, numero_rue) VALUES 
('France', 'Paris', 'Champs-Élysées', 101),
('France', 'Marseille', 'Rue de la République', 50),
('France', 'Lyon', 'Rue de la République', 50),
('France', 'Marseille', 'Rue de la République', 50),
('France', 'Paris', 'Avenue George V', 101);

INSERT INTO agence (nom, password, adresse_id) VALUES 
('Agence Parisienne', 'password123', 2),
('Agence Marseillaise', 'password456', 3);

INSERT INTO hotel (nom, adresse_id, nb_etoiles, nb_lits_disponibles) VALUES 
('Hôtel de Paris', 5, 4, 50),
('Hôtel de Marseille', 4, 3, 30);

INSERT INTO client (nom, prenom, email) VALUES 
('Dupont', 'Jean', 'jean.dupont@example.com');

INSERT INTO carte_bancaire (numero, cvv, date_expiration, client_id) VALUES 
(1234567890123456, 123, '12/24', 1),
(9876543210123456, 456, '10/25', 1);

INSERT INTO agence_client (agence_id, client_id) VALUES 
(1, 1),
(2, 1);

INSERT INTO agence_hotel (agence_id, hotel_id) VALUES 
(1, 1),
(1, 2),
(2, 1);



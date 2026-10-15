-- Sistema de Gestion Odontologica - creacion de la base (semana 6)
-- Ejecutar una sola vez en MySQL Workbench antes de iniciar el backend.
--
-- Las tablas NO se crean a mano: Hibernate las genera a partir de las entidades
-- (spring.jpa.hibernate.ddl-auto=update), con sus claves foraneas y restricciones.
-- Al primer arranque, DatosIniciales carga una clinica de ejemplo.

CREATE DATABASE IF NOT EXISTS odontologia_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE odontologia_db;

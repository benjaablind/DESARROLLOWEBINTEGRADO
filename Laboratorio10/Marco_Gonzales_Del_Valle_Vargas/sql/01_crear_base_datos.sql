-- Laboratorio 06 - seccion 7 (vigente en la semana 10)
-- Ejecutar una sola vez antes de iniciar la aplicacion.
-- Las tablas NO se crean a mano: Hibernate las genera desde las entidades
-- gracias a spring.jpa.hibernate.ddl-auto=update.
--
-- En PowerShell con XAMPP:
--   C:\xampp\mysql\bin\mysql.exe -u root -e "source sql/01_crear_base_datos.sql"

CREATE DATABASE IF NOT EXISTS productos_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE productos_db;

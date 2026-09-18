-- Laboratorio 06 - secciones 14 y 16
-- Ejecutar despues de levantar la aplicacion con mvn spring-boot:run.

USE productos_db;

SHOW TABLES;
DESCRIBE productos;
SELECT * FROM productos;

-- Comprobacion de persistencia (seccion 16):
-- 1. Crear dos productos desde Postman.
-- 2. Ejecutar el SELECT de arriba.
-- 3. Detener y volver a iniciar Spring Boot.
-- 4. Ejecutar GET /api/productos: los productos siguen existiendo.

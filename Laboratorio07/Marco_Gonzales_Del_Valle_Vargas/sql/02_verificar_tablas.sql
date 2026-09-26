-- Laboratorio 07 - verificacion del esquema
-- Ejecutar despues de levantar la aplicacion con mvn spring-boot:run.
-- Hibernate crea movimientos_stock y le agrega la FK hacia productos.

USE productos_db;

SHOW TABLES;
DESCRIBE productos;
DESCRIBE movimientos_stock;

SELECT * FROM productos;
SELECT * FROM movimientos_stock ORDER BY id DESC;

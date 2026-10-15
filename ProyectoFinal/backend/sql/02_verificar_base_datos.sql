-- Sistema de Gestion Odontologica - verificacion (semanas 6 a 9)
-- Ejecutar despues del primer arranque del backend.

USE odontologia_db;

-- 1. Tablas que genero Hibernate (14).
SHOW TABLES;

-- 2. Claves foraneas: la integridad referencial que no existia en memoria.
SELECT table_name AS tabla, column_name AS columna,
       referenced_table_name AS referencia
FROM information_schema.key_column_usage
WHERE table_schema = 'odontologia_db' AND referenced_table_name IS NOT NULL
ORDER BY table_name, column_name;

-- 3. Contrasenas: solo hashes BCrypt ($2a$10$...), nunca el texto original.
SELECT id, usuario, rol, activo, password FROM usuarios;

-- 4. Importes exactos: DECIMAL(12,2), no coma flotante.
DESCRIBE tratamientos;
DESCRIBE pagos;

-- 5. Saldo de cada tratamiento calculado en SQL (debe coincidir con la API).
SELECT t.id, t.nombre, t.precio,
       COALESCE(SUM(p.monto), 0) AS pagado,
       t.precio - COALESCE(SUM(p.monto), 0) AS saldo
FROM tratamientos t
LEFT JOIN pagos p ON p.tratamiento_id = t.id
GROUP BY t.id, t.nombre, t.precio
ORDER BY t.id;

-- 6. Persistencia: detener el backend, volver a iniciarlo y repetir estas
--    consultas. Los datos siguen ahi; con el repositorio en memoria se perdian.

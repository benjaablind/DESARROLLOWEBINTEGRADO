-- Laboratorio 08 - seccion 12: tablas de seguridad
-- Ejecutar despues de levantar la aplicacion: DatosSeguridadIniciales crea
-- los roles y los usuarios "usuario" y "admin" si todavia no existen.

USE productos_db;

SHOW TABLES;

-- La columna password debe mostrar hashes BCrypt ($2a$10$...),
-- nunca las contrasenas originales.
SELECT id, username, activo, password FROM usuarios;

SELECT * FROM roles;
SELECT * FROM usuarios_roles;

-- Vista legible: que roles tiene cada usuario.
SELECT u.username, u.activo, r.nombre AS rol
FROM usuarios u
JOIN usuarios_roles ur ON ur.usuario_id = u.id
JOIN roles r ON r.id = ur.rol_id
ORDER BY u.username, r.nombre;

-- Laboratorio 07 - seccion 12: datos de prueba
-- Alternativa al POST /api/productos del CRUD de la semana 6.

USE productos_db;

INSERT INTO productos (nombre, categoria, precio, stock) VALUES
('Laptop Lenovo ThinkPad', 'Tecnologia', 4200.00, 8),
('Mouse Logitech MX',      'Tecnologia',  320.00, 20),
('Silla ergonomica',       'Muebles',     850.00, 6),
('Escritorio ejecutivo',   'Muebles',    1200.00, 4),
('Monitor 27 pulgadas',    'Tecnologia', 1450.00, 10);

SELECT id, nombre, categoria, precio, stock FROM productos;

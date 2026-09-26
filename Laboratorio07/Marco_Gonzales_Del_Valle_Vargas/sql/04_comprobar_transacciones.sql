-- Laboratorio 07 - secciones 14 y 15: commit y rollback
-- Ejecutar por bloques, alternando con las llamadas de Postman.

USE productos_db;

-- 1) ANTES de la salida: anotar stock y numero de movimientos.
SELECT id, nombre, stock FROM productos WHERE id = 1;
SELECT COUNT(*) AS movimientos FROM movimientos_stock WHERE producto_id = 1;

-- 2) POST http://localhost:8080/api/productos/1/salidas?cantidad=2
--    Responde 200. Verificar: el stock bajo en 2 y hay un movimiento SALIDA.
SELECT id, nombre, stock FROM productos WHERE id = 1;
SELECT id, producto_id, tipo, cantidad, fecha
FROM movimientos_stock
ORDER BY id DESC;

-- 3) POST http://localhost:8080/api/productos/1/salidas/simular-error?cantidad=1
--    Responde 500. Las dos consultas siguientes deben devolver EXACTAMENTE
--    los mismos valores del paso 2: la excepcion provoco rollback y no quedo
--    ningun cambio parcial (ni el descuento de stock ni el movimiento).
SELECT id, nombre, stock FROM productos WHERE id = 1;
SELECT COUNT(*) AS movimientos FROM movimientos_stock WHERE producto_id = 1;

-- 4) POST http://localhost:8080/api/productos/1/entradas?cantidad=5
--    Responde 200. El stock sube y aparece un movimiento de tipo ENTRADA.
SELECT id, nombre, stock FROM productos WHERE id = 1;
SELECT id, producto_id, tipo, cantidad, fecha
FROM movimientos_stock
WHERE producto_id = 1
ORDER BY id DESC;

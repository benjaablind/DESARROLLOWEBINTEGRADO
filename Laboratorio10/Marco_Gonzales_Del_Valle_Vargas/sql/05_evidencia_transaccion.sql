-- Laboratorio 10 - seccion 12: evidencia de commit y rollback.
--
-- Ejecutar ANTES y DESPUES de cada salida de inventario y comparar:
--   1. POST /api/inventario/1/salida?cantidad=2      -> stock baja en 2 y hay 1 movimiento nuevo (commit)
--   2. POST /api/inventario/1/salida?cantidad=99999  -> 409 y NADA cambia (rollback)
--
-- El rollback no se demuestra con el mensaje de error: se demuestra
-- comparando el estado de la base antes y despues del fallo.

USE productos_db;

SELECT id, nombre, stock FROM productos WHERE id = 1;

SELECT COUNT(*) AS movimientos_del_producto
FROM movimientos_stock
WHERE producto_id = 1;

SELECT id, tipo, cantidad, fecha, producto_id
FROM movimientos_stock
WHERE producto_id = 1
ORDER BY id DESC;

-- Resumen equivalente a GET /api/admin/resumen
SELECT
    (SELECT COUNT(*) FROM productos)             AS total_productos,
    (SELECT COUNT(*) FROM productos WHERE stock < 5) AS productos_con_stock_bajo,
    (SELECT COUNT(*) FROM movimientos_stock)     AS total_movimientos;

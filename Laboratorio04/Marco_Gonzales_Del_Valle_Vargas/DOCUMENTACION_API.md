# Documentación de la API — Catálogo de Productos

Ejercicio complementario 3 de la Guía de Laboratorio 04.

**URL base:** `http://localhost:8080/api/productos`
**Formato:** JSON (`Content-Type: application/json` en POST, PUT y PATCH)

## Endpoints

| # | Método | URL | Body esperado | Respuesta | Descripción |
|---|--------|-----|---------------|-----------|-------------|
| 1 | GET | `/api/productos` | — | 200 OK, lista de productos | Lista todos los productos ordenados por id. |
| 2 | GET | `/api/productos?categoria=Tecnologia` | — | 200 OK, lista filtrada | Filtra por categoría sin distinguir mayúsculas. Si no hay coincidencias devuelve `[]`. |
| 3 | GET | `/api/productos/{id}` | — | 200 OK, producto · 404 si no existe | Busca un producto por su identificador. |
| 4 | GET | `/api/productos/buscar?texto=lap` | — | 200 OK, lista de productos | Ejercicio 1. Devuelve los productos cuyo nombre contiene el texto (parcial, sin distinguir mayúsculas). |
| 5 | POST | `/api/productos` | `{ "nombre", "categoria", "precio", "stock" }` | 201 Created, producto creado + header `Location` · 400 si hay datos inválidos | Crea un producto. El id lo asigna el servidor. |
| 6 | PUT | `/api/productos/{id}` | `{ "nombre", "categoria", "precio", "stock" }` (todos los campos) | 200 OK, producto actualizado · 400 · 404 | Reemplaza por completo el producto. |
| 7 | PATCH | `/api/productos/{id}/stock` | `{ "stock": 20 }` | 200 OK, producto actualizado · 400 · 404 | Actualiza únicamente el stock. |
| 8 | PATCH | `/api/productos/{id}/disminuir-stock` | `{ "cantidad": 3 }` | 200 OK, producto actualizado · 400 si el stock quedaría negativo · 404 | Ejercicio 2. Descuenta unidades del stock actual. |
| 9 | DELETE | `/api/productos/{id}` | — | 204 No Content · 404 si no existe | Elimina el producto. |

## Modelo de Producto (respuesta)

```json
{
  "id": 1,
  "nombre": "Laptop Lenovo",
  "categoria": "Tecnologia",
  "precio": 3500.0,
  "stock": 10
}
```

## Reglas de validación (400 Bad Request)

| Campo | Regla | Mensaje |
|-------|-------|---------|
| nombre | No vacío | El nombre es obligatorio |
| categoria | No vacía | La categoria es obligatoria |
| precio | Obligatorio y mayor a cero | El precio es obligatorio / El precio debe ser mayor a cero |
| stock | Obligatorio y mayor o igual a cero | El stock es obligatorio / El stock no puede ser negativo |
| cantidad (disminuir-stock) | Obligatoria y mayor a cero | La cantidad es obligatoria / La cantidad debe ser mayor a cero |

Además se responde 400 cuando el body no es JSON válido, cuando un campo tiene un tipo incorrecto o cuando el `{id}` de la ruta no es numérico.

## Formato de error

Todos los errores controlados devuelven la misma estructura:

```json
{
  "estado": 404,
  "mensaje": "No existe un producto con id: 999",
  "ruta": "/api/productos/999",
  "fechaHora": "2026-09-03T15:30:00"
}
```

## Ejemplos con curl (Windows: usar `curl.exe`)

```bash
# Listar
curl http://localhost:8080/api/productos

# Filtrar por categoría
curl "http://localhost:8080/api/productos?categoria=Tecnologia"

# Buscar por texto (ejercicio 1)
curl "http://localhost:8080/api/productos/buscar?texto=lap"

# Crear
curl -X POST http://localhost:8080/api/productos -H "Content-Type: application/json" -d "{\"nombre\":\"Audifonos\",\"categoria\":\"Tecnologia\",\"precio\":150.0,\"stock\":15}"

# Actualizar completo
curl -X PUT http://localhost:8080/api/productos/1 -H "Content-Type: application/json" -d "{\"nombre\":\"Laptop Lenovo ThinkPad\",\"categoria\":\"Tecnologia\",\"precio\":3899.90,\"stock\":12}"

# Actualizar stock
curl -X PATCH http://localhost:8080/api/productos/1/stock -H "Content-Type: application/json" -d "{\"stock\":30}"

# Disminuir stock (ejercicio 2)
curl -X PATCH http://localhost:8080/api/productos/1/disminuir-stock -H "Content-Type: application/json" -d "{\"cantidad\":3}"

# Eliminar
curl -X DELETE http://localhost:8080/api/productos/1
```

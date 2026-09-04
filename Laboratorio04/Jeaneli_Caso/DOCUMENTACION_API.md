# Documentación de la API — Catálogo de Productos

Ejercicio 3 (sección 28 de la Guía de Laboratorio N.° 04): documentación manual de todos los endpoints implementados.

URL base: `http://localhost:8080`

| Método | Endpoint | Body esperado | Código de respuesta | Descripción |
|---|---|---|---|---|
| GET | `/api/productos` | No aplica | 200 OK | Lista todos los productos. Acepta el parámetro opcional `?categoria=` para filtrar por categoría exacta (sin distinguir mayúsculas/minúsculas). |
| GET | `/api/productos/{id}` | No aplica | 200 OK / 404 Not Found | Devuelve un producto por su id. Si no existe, responde 404 con un `ErrorResponse` en JSON. |
| GET | `/api/productos/buscar?texto=` | No aplica | 200 OK | Ejercicio 1: devuelve los productos cuyo nombre contiene el texto indicado (búsqueda parcial, sin distinguir mayúsculas/minúsculas). |
| POST | `/api/productos` | `{"nombre": "string", "categoria": "string", "precio": number, "stock": number}` | 201 Created / 400 Bad Request | Crea un producto nuevo. Responde con el producto creado y el header `Location` apuntando a `/api/productos/{id}`. Si los datos no pasan las validaciones (`nombre`/`categoria` vacíos, `precio` no positivo, `stock` negativo o ausente), responde 400. |
| PUT | `/api/productos/{id}` | `{"nombre": "string", "categoria": "string", "precio": number, "stock": number}` | 200 OK / 400 Bad Request / 404 Not Found | Reemplaza completamente los datos de un producto existente. |
| PATCH | `/api/productos/{id}/stock` | `{"stock": number}` | 200 OK / 400 Bad Request / 404 Not Found | Actualiza únicamente el stock del producto, fijándolo al valor enviado. |
| PATCH | `/api/productos/{id}/disminuir-stock` | `{"cantidad": number}` | 200 OK / 400 Bad Request / 404 Not Found | Ejercicio 2: descuenta `cantidad` unidades del stock actual. Si `cantidad` es mayor que el stock disponible, responde 400 Bad Request sin modificar el producto. |
| DELETE | `/api/productos/{id}` | No aplica | 204 No Content / 404 Not Found | Elimina un producto por su id. |

## Formato de error estándar

Todos los errores controlados (404 y 400) devuelven el mismo formato JSON, gracias a `ApiExceptionHandler`:

```json
{
  "estado": 404,
  "mensaje": "No existe un producto con id: 999",
  "ruta": "/api/productos/999",
  "fechaHora": "2026-09-04T15:30:00"
}
```

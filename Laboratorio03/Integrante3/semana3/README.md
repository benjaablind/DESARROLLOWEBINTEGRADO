# Laboratorio 03 - TDD en Spring Boot

API REST de productos desarrollada aplicando el ciclo **Red-Green-Refactor**.

## Cómo ejecutar

```bash
mvn clean test        # ejecutar todas las pruebas
mvn spring-boot:run   # levantar la aplicación en http://localhost:8080
```

## Endpoints

| Método | Ruta              | Descripción                       |
|--------|-------------------|------------------------------------|
| GET    | `/productos`      | Lista todos los productos          |
| GET    | `/productos/{id}` | Busca un producto por id (404 si no existe) |
| POST   | `/productos`      | Registra un producto (201)         |
| PUT    | `/productos/{id}` | Actualiza un producto existente    |
| DELETE | `/productos/{id}` | Elimina un producto existente      |

## Reglas de negocio

- El nombre no puede estar vacío.
- El precio debe ser mayor que cero.
- El stock no puede ser negativo.

## Pruebas

- `ProductoServiceTest`: pruebas unitarias de la capa de servicio (registro, validaciones, búsqueda, actualización, eliminación).
- `ProductoControllerTest`: pruebas de la capa web con `MockMvc` y `@MockitoBean` sobre `ProductoService`.

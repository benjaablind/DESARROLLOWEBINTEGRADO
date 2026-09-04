# Laboratorio 04 - Marco Gonzales del Valle Vargas

API REST **Catálogo de Productos** construida con Spring Boot según la Guía de Laboratorio 04
(Implementación de API REST, herramientas de prueba y métodos HTTP).

## Tecnologías

- Java 25
- Spring Boot 3.5.6 (Spring Web, Validation, DevTools, Starter Test)
- Maven (incluye Maven Wrapper: `mvnw` / `mvnw.cmd`)
- JUnit 5 + MockMvc

## Estructura

```
src/main/java/com/utp/semana4
├── Semana4ApiRestApplication.java
├── controller
│   └── ProductoController.java          → endpoints /api/productos
├── dto
│   ├── ProductoRequest.java             → POST y PUT (con validaciones)
│   ├── ActualizarStockRequest.java      → PATCH /stock
│   ├── DisminuirStockRequest.java       → PATCH /disminuir-stock (ejercicio 2)
│   └── ErrorResponse.java               → JSON uniforme de error
├── exception
│   ├── ProductoNoEncontradoException.java  → 404
│   ├── StockInsuficienteException.java     → 400 (ejercicio 2)
│   └── ApiExceptionHandler.java            → @RestControllerAdvice
├── model
│   └── Producto.java
└── service
    └── ProductoService.java             → lógica de negocio, almacenamiento en memoria

src/test/java/com/utp/semana4
└── ProductoControllerTest.java          → 13 pruebas con MockMvc
```

## Endpoints

| Método | URL | Respuesta |
|--------|-----|-----------|
| GET | `/api/productos` | 200 |
| GET | `/api/productos?categoria=Tecnologia` | 200 |
| GET | `/api/productos/{id}` | 200 / 404 |
| GET | `/api/productos/buscar?texto=lap` | 200 (ejercicio 1) |
| POST | `/api/productos` | 201 + `Location` / 400 |
| PUT | `/api/productos/{id}` | 200 / 400 / 404 |
| PATCH | `/api/productos/{id}/stock` | 200 / 400 / 404 |
| PATCH | `/api/productos/{id}/disminuir-stock` | 200 / 400 / 404 (ejercicio 2) |
| DELETE | `/api/productos/{id}` | 204 / 404 |

La documentación completa (bodies, códigos, ejemplos curl) está en [DOCUMENTACION_API.md](DOCUMENTACION_API.md) (ejercicio 3).
Las preguntas de reflexión están respondidas en [RESPUESTAS_REFLEXION.md](RESPUESTAS_REFLEXION.md).

## Ejecutar

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api/productos` con 3 productos precargados.

## Pruebas

```bash
mvn test
```

## Empaquetar como JAR

```bash
mvn clean package
java -jar target/semana4-api-rest-0.0.1-SNAPSHOT.jar
```

Si `mvn` no está en el PATH, se puede usar el wrapper: `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS.

## Checklist de verificación (sección 26 de la guía)

| N.º | Verificación | Cumple |
|-----|--------------|--------|
| 1 | El proyecto compila sin errores | Sí |
| 2 | La aplicación inicia en el puerto 8080 | Sí |
| 3 | GET /api/productos devuelve una lista JSON | Sí |
| 4 | GET /api/productos/{id} devuelve un producto existente | Sí |
| 5 | POST /api/productos crea un producto y devuelve 201 | Sí |
| 6 | PUT /api/productos/{id} actualiza un producto completo | Sí |
| 7 | PATCH /api/productos/{id}/stock actualiza solo el stock | Sí |
| 8 | DELETE /api/productos/{id} devuelve 204 | Sí |
| 9 | Los datos inválidos devuelven 400 | Sí |
| 10 | Un producto inexistente devuelve 404 | Sí |
| 11 | mvn test ejecuta las pruebas correctamente | Sí |
| 12 | mvn clean package genera el JAR | Sí |

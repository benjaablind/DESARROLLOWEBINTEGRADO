# semana4-api-rest — Catálogo de Productos (Guía de Laboratorio N.° 04)

Proyecto Spring Boot generado a partir de la **Guía de Laboratorio N.° 04** del curso
Desarrollo Web Integrado (Semana 4): API REST, herramientas de prueba y métodos HTTP.

## Qué incluye

- Estructura por capas según la guía: `controller`, `dto`, `exception`, `model`, `service`.
- CRUD completo con los métodos HTTP correctos: `GET /api/productos`, `GET /api/productos/{id}`,
  `POST /api/productos`, `PUT /api/productos/{id}`, `PATCH /api/productos/{id}/stock`,
  `DELETE /api/productos/{id}`.
- Validaciones con Jakarta Validation (`@NotBlank`, `@NotNull`, `@Positive`, `@Min`) en los DTO.
- Manejo global de errores con `@RestControllerAdvice` (`ApiExceptionHandler`), con formato
  JSON uniforme (`ErrorResponse`) para 400 y 404.
- Test con MockMvc (`ProductoControllerTest`).
- **Ejercicios complementarios (sección 28) ya resueltos:**
  1. `GET /api/productos/buscar?texto=lap` — búsqueda por nombre parcial.
  2. `PATCH /api/productos/{id}/disminuir-stock` — disminuye stock sin permitir que quede negativo
     (400 Bad Request si la cantidad a disminuir supera el stock actual).
  3. `DOCUMENTACION_API.md` — tabla con todos los endpoints, body esperado y códigos de respuesta.
- `RESPUESTAS_REFLEXION.md` con las 10 preguntas de la sección 29 respondidas.

## Cómo abrirlo en VS Code

1. Copia esta carpeta dentro de tu espacio de trabajo (por ejemplo junto a `semana2/`).
2. Ábrela en VS Code (`code .` o *File > Open Folder*).
3. Abre una terminal integrada y ejecuta:

   ```bash
   mvn spring-boot:run
   ```

   o usa el botón **Run** sobre `main()` en `Semana4ApiRestApplication.java` si `mvn` no está
   disponible en el PATH de esa PC.

> **Puerto ocupado:** si en tu PC (por ejemplo una de laboratorio) el puerto 8080 ya está en uso
> por otra herramienta (Jenkins, Tomcat, etc.), cambia `server.port` en
> `src/main/resources/application.properties` a uno libre (8090, 8091...) antes de correr.

4. Prueba el listado desde el navegador:

   ```
   http://localhost:8080/api/productos
   ```

5. Usa Postman, Thunder Client o `curl` para probar POST, PUT, PATCH y DELETE (sección 21 y 22
   de la guía tienen los ejemplos exactos).

## Checklist de verificación (sección 26 de la guía)

- El proyecto compila sin errores.
- La aplicación inicia en el puerto configurado.
- `GET /api/productos` devuelve una lista JSON.
- `GET /api/productos/{id}` devuelve un producto existente.
- `POST /api/productos` crea un producto y devuelve 201.
- `PUT /api/productos/{id}` actualiza un producto completo.
- `PATCH /api/productos/{id}/stock` actualiza solo el stock.
- `DELETE /api/productos/{id}` devuelve 204.
- Los datos inválidos devuelven 400.
- Un producto inexistente devuelve 404.
- `mvn test` ejecuta las pruebas correctamente.
- `mvn clean package` genera el JAR.

## Aviso sobre la compilación

Este proyecto fue construido en un entorno sin salida de red hacia Maven Central, así que no
se pudo ejecutar `mvn clean package` ahí para una verificación 100% automática. El código sigue
al pie de la letra los fragmentos ya probados de la guía, más las extensiones de los ejercicios
complementarios (búsqueda por texto y disminución de stock), que son lógica Java estándar de bajo
riesgo. Aun así, corre `mvn test` y `mvn spring-boot:run` en tu VS Code como primer paso para
confirmarlo en tu máquina.

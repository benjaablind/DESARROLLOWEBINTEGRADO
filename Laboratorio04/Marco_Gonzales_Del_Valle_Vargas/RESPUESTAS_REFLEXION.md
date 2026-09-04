# Preguntas de reflexión — Laboratorio 04

**1. ¿Por qué GET no debe usarse para crear, actualizar o eliminar datos?**
GET está definido como un método seguro e idempotente: solo consulta y no debe cambiar el estado del servidor. Navegadores, proxies y buscadores asumen eso, por lo que pueden cachear, repetir o prellamar una URL GET. Si un GET borrara datos, un simple enlace o un refresco podría destruir información sin intención del usuario.

**2. ¿Qué diferencia existe entre POST y PUT?**
POST crea un recurso nuevo en la colección y el servidor decide su identificador (responde 201 con el header `Location`). PUT se envía a la URL de un recurso concreto y reemplaza su representación completa; es idempotente: repetir el mismo PUT deja el mismo resultado, mientras que repetir un POST crea varios recursos.

**3. ¿Qué diferencia existe entre PUT y PATCH?**
PUT reemplaza el recurso entero, por eso el cliente debe enviar todos los campos. PATCH aplica una modificación parcial: solo se envían los campos que cambian, como en `PATCH /api/productos/1/stock` con `{ "stock": 20 }`.

**4. ¿Por qué es útil ResponseEntity en una API REST?**
Permite controlar de forma explícita el código de estado (200, 201, 204, 404), los headers (como `Location`) y el cuerpo de la respuesta. Sin él, el controlador devolvería siempre 200 y no podría expresar correctamente el resultado de la operación.

**5. ¿Qué función cumple @RequestBody?**
Indica a Spring que tome el cuerpo de la solicitud HTTP (JSON) y lo convierta, mediante Jackson, en un objeto Java como `ProductoRequest`. Junto con `@Valid` activa las validaciones declaradas en ese objeto.

**6. ¿Qué diferencia hay entre @PathVariable y @RequestParam?**
`@PathVariable` lee un valor que forma parte de la ruta e identifica un recurso: el `1` en `/api/productos/1`. `@RequestParam` lee un parámetro de consulta después del `?`, usado para filtrar u opciones: `categoria` en `/api/productos?categoria=Tecnologia`.

**7. ¿Por qué conviene usar DTOs en lugar de recibir directamente cualquier objeto?**
El DTO define exactamente qué campos acepta la API y qué validaciones aplican, sin exponer el modelo interno. Así el cliente no puede enviar campos que no le corresponden (por ejemplo el `id`), se evita acoplar la API a la estructura interna y se centralizan las reglas de validación.

**8. ¿Qué significa devolver un error 400?**
Bad Request: la solicitud del cliente es inválida (campos vacíos, precio negativo, JSON mal formado, tipos incorrectos o una regla de negocio incumplida como stock insuficiente). El servidor la rechaza y el cliente debe corregirla antes de reintentar.

**9. ¿Qué significa devolver un error 404?**
Not Found: la URL apunta a un recurso que no existe, por ejemplo `/api/productos/999` cuando no hay producto con id 999. La solicitud está bien formada, pero no hay nada que devolver.

**10. ¿Qué ventaja tiene centralizar errores con @RestControllerAdvice?**
Un solo lugar traduce excepciones a respuestas HTTP, evitando repetir try-catch en cada endpoint. Todos los errores comparten el mismo formato JSON (`estado`, `mensaje`, `ruta`, `fechaHora`), lo que simplifica el manejo en el cliente y facilita agregar nuevos tipos de error sin tocar los controladores.

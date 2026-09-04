# Preguntas de reflexión — Guía de Laboratorio N.° 04

**Curso:** Desarrollo Web Integrado (100000ST61) — Semana 4

## 1. ¿Por qué GET no debe usarse para crear, actualizar o eliminar datos?

Porque según la semántica HTTP, GET es un método **seguro** (no debe producir efectos secundarios en el servidor) e **idempotente** (repetirlo varias veces produce siempre el mismo resultado, sin cambiar el estado del sistema). Además, los navegadores, proxies y buscadores pueden precargar o cachear peticiones GET automáticamente, así que si GET creara o eliminara datos, esas acciones podrían dispararse sin que el usuario lo pidiera explícitamente (por ejemplo, un crawler siguiendo enlaces). Usar POST, PUT, PATCH o DELETE para modificar datos deja clara la intención de la operación y evita estos efectos accidentales.

## 2. ¿Qué diferencia existe entre POST y PUT?

POST se usa para **crear** un recurso nuevo cuyo identificador normalmente no conoce el cliente; el servidor decide el id y lo informa (en esta práctica, mediante el header `Location`). PUT se usa para **reemplazar por completo** un recurso que ya existe en una URI conocida (`/api/productos/{id}`): el cliente envía todos los campos del recurso, y el servidor sustituye el objeto entero por lo recibido. Otra diferencia importante es la idempotencia: hacer el mismo PUT varias veces deja el recurso en el mismo estado final, mientras que repetir el mismo POST puede crear varios recursos distintos.

## 3. ¿Qué diferencia existe entre PUT y PATCH?

PUT reemplaza el recurso completo: si se omite un campo en el body, ese campo se pierde o queda en su valor por defecto. PATCH se usa para una **actualización parcial**: solo se modifican los campos enviados, dejando el resto del recurso intacto. En esta guía, `PUT /api/productos/{id}` exige nombre, categoría, precio y stock completos, mientras que `PATCH /api/productos/{id}/stock` (y el `PATCH /api/productos/{id}/disminuir-stock` del Ejercicio 2) solo tocan el campo `stock`, sin necesidad de reenviar todo el producto.

## 4. ¿Por qué es útil ResponseEntity en una API REST?

`ResponseEntity<T>` permite controlar explícitamente tres partes de la respuesta HTTP: el código de estado (200, 201, 204, 400, 404, etc.), las cabeceras (como `Location` al crear un recurso) y el cuerpo de la respuesta. Sin `ResponseEntity`, un método de controlador que solo devuelve el objeto (por ejemplo `Producto`) siempre respondería con 200 OK, lo cual no alcanza para expresar operaciones como "recurso creado" (201) o "eliminado sin contenido que devolver" (204). Usar `ResponseEntity` hace que la API cumpla realmente con las convenciones REST de códigos de estado.

## 5. ¿Qué función cumple @RequestBody?

`@RequestBody` le indica a Spring que debe tomar el cuerpo de la petición HTTP (normalmente JSON) y convertirlo automáticamente en un objeto Java, usando Jackson por debajo. Es lo que permite que un método como `crear(@RequestBody ProductoRequest request)` reciba directamente un objeto `ProductoRequest` ya poblado, en vez de que el programador tenga que leer y parsear el JSON manualmente.

## 6. ¿Qué diferencia hay entre @PathVariable y @RequestParam?

`@PathVariable` extrae un valor que forma parte de la ruta de la URL y que identifica un recurso específico, como el `{id}` en `/api/productos/{id}`. `@RequestParam` extrae un valor enviado como parámetro de consulta después del `?`, normalmente usado para filtros u opciones que no identifican un recurso único, como `categoria` en `/api/productos?categoria=Tecnologia` o `texto` en `/api/productos/buscar?texto=lap`.

## 7. ¿Por qué conviene usar DTOs en lugar de recibir directamente cualquier objeto?

Un DTO (Data Transfer Object) define exactamente qué campos puede enviar o recibir el cliente, independientemente de cómo esté modelada la clase interna del sistema. Esto evita varios problemas: que el cliente pueda enviar campos que no debería controlar (como el `id`, que en esta práctica lo genera el servidor), que se filtren campos internos que no deberían exponerse, y permite aplicar anotaciones de validación (`@NotBlank`, `@Positive`, `@Min`) solo sobre los datos de entrada sin ensuciar el modelo de dominio. En proyectos reales, además, el DTO puede evolucionar de forma independiente al modelo interno sin romper el contrato de la API.

## 8. ¿Qué significa devolver un error 400?

`400 Bad Request` indica que la solicitud enviada por el cliente tiene datos inválidos, incompletos o mal formados, y que el servidor no puede procesarla tal como llegó. En esta práctica se devuelve 400 cuando falla una validación del DTO (por ejemplo, nombre vacío, precio negativo o cero, stock ausente) o cuando se viola una regla de negocio manual, como intentar disminuir más stock del que existe en el Ejercicio 2. Es responsabilidad del cliente corregir la solicitud antes de reintentarla.

## 9. ¿Qué significa devolver un error 404?

`404 Not Found` indica que el recurso solicitado no existe en el servidor, aunque la solicitud en sí esté bien formada. En esta práctica ocurre cuando se busca, actualiza o elimina un producto cuyo id no está registrado (`ProductoNoEncontradoException`). A diferencia del 400, aquí el problema no es la forma de la solicitud sino la identidad del recurso al que apunta.

## 10. ¿Qué ventaja tiene centralizar errores con @RestControllerAdvice?

`@RestControllerAdvice` permite manejar las excepciones de todos los controladores en un solo lugar (`ApiExceptionHandler`), en vez de repetir bloques `try/catch` en cada método de cada controlador. Esto tiene varias ventajas: el código de los controladores queda más limpio y enfocado solo en la lógica HTTP; todas las respuestas de error siguen el mismo formato JSON (`ErrorResponse`), lo cual facilita que el cliente (Angular, Postman, etc.) las procese de manera uniforme; y si se necesita cambiar cómo se reporta un tipo de error, solo hay que modificarlo en un lugar para que el cambio aplique a toda la API.

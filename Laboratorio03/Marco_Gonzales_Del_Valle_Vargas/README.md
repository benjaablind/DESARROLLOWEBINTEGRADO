# Laboratorio 03 - Marco Gonzales del Valle Vargas

API REST de productos construida con **TDD (Red-Green-Refactor)** sobre Spring Boot.

## Tecnologías

- Java 25
- Spring Boot 3.5.6
- Maven
- JUnit 5 + AssertJ + Mockito + MockMvc (`spring-boot-starter-test`)

## Estructura

```
src/main/java/com/example/laboratorio3
├── Laboratorio3Application.java
├── controller
│   ├── ProductoController.java      → expone HTTP
│   └── ManejadorDeErrores.java      → traduce IllegalArgumentException a 400
├── model
│   └── Producto.java                → datos
└── service
    └── ProductoService.java         → reglas de negocio (almacenamiento en memoria)

src/test/java/com/example/laboratorio3
├── Laboratorio3ApplicationTests.java   → el contexto Spring inicia
├── controller/ProductoControllerTest.java  → @WebMvcTest + MockMvc (9 pruebas)
└── service/ProductoServiceTest.java        → prueba unitaria pura (11 pruebas)
```

## Reglas de negocio

| Regla | Mensaje de error |
|---|---|
| El nombre no puede estar vacío | `El nombre es obligatorio` |
| El precio debe ser mayor que cero | `El precio debe ser mayor que cero` |
| El stock no puede ser negativo | `El stock no puede ser negativo` |

Las validaciones viven **solo en el servicio**; el controlador únicamente traduce a HTTP.

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/productos` | 200 + lista JSON |
| GET | `/productos/{id}` | 200 + producto / 404 si no existe |
| POST | `/productos` | 201 + producto con id asignado / 400 si viola una regla |
| PUT | `/productos/{id}` | 200 + producto actualizado / 404 si no existe / 400 si viola una regla |
| DELETE | `/productos/{id}` | 204 sin cuerpo / 404 si no existe |

## Ejecución

```bash
mvn clean test        # 26 pruebas, BUILD SUCCESS
mvn spring-boot:run   # http://localhost:8080/productos
```

## Bitácora del ciclo TDD

El código se escribió siguiendo el ciclo, ejecutando `mvn test` en cada fase.

### Ciclo 1 — registrar y listar

**RED.** Se escribió `registrarProductoValido_debeAsignarIdYGuardar` cuando `ProductoService` todavía no existía:

```
[ERROR] ProductoServiceTest.java:[12,9] cannot find symbol
  symbol:   class ProductoService
[ERROR] Failed to execute goal ...:testCompile ... Compilation failure
```

**GREEN.** Se creó `ProductoService` con lo mínimo: una `List` en memoria, una `secuencia` para el id, `registrar()` y `listar()`.

```
Tests run: 6, Failures: 0, Errors: 0 — BUILD SUCCESS
```

### Ciclo 2 — validaciones

**RED.** Se agregaron las tres pruebas de reglas de negocio antes de tocar el servicio:

```
[ERROR] Tests run: 4, Failures: 3, Errors: 0 <<< FAILURE! -- in ProductoServiceTest
[ERROR]   registrarProductoConPrecioCero_debeLanzarExcepcion:28
[ERROR]   registrarProductoConStockNegativo_debeLanzarExcepcion:48
[ERROR]   registrarProductoSinNombre_debeLanzarExcepcion:38
[INFO] BUILD FAILURE
```

**GREEN + REFACTOR.** Se implementaron las validaciones y, en lugar de amontonar los `if` dentro de
`registrar()`, se extrajeron al método privado `validar()`. El comportamiento no cambió: las mismas
pruebas siguieron pasando, que es justamente lo que permite refactorizar sin miedo.

```
Tests run: 9, Failures: 0, Errors: 0 — BUILD SUCCESS
```

### Ciclo 3 — capa web

**RED.** Se escribió `ProductoControllerTest` con `@WebMvcTest` y `@MockitoBean` antes de crear el controlador:

```
[ERROR] ProductoControllerTest.java:[22,13] cannot find symbol
  symbol: class ProductoController
```

**GREEN.** Se implementó `ProductoController` con `GET /productos`, `GET /productos/{id}` y `POST /productos`.

```
Tests run: 13, Failures: 0, Errors: 0 — BUILD SUCCESS
```

### Ciclo 4 — ejercicios complementarios

**RED.** Pruebas de `eliminar()`, `actualizar()`, los endpoints DELETE/PUT y la respuesta 400:

```
[ERROR] Tests run: 25, Failures: 0, Errors: 16
[ERROR]   ProductoControllerTest.eliminarProductoExistente_debeRetornar204:112 Unresolved compilation problem
[ERROR]   ProductoControllerTest.actualizarProductoExistente_...:128 Unresolved compilation problem
[ERROR]   ProductoControllerTest.registrarProductoInvalido_debeRetornar400ConElMensaje:103
            Servlet Request processing failed: IllegalArgumentException: El precio debe ser mayor que cero
[INFO] BUILD FAILURE
```

Nótese el tercer fallo: sin manejador de errores, una regla de negocio incumplida se escapaba como
error 500. La prueba lo delató antes de que llegara a Postman.

**GREEN.** Se implementaron `eliminar()`, `actualizar()`, los endpoints `DELETE`/`PUT` y
`ManejadorDeErrores`, que traduce `IllegalArgumentException` a `400 Bad Request`.

```
Tests run: 26, Failures: 0, Errors: 0 — BUILD SUCCESS
```

## Evidencia de ejecución de la API

Comprobación real con la aplicación levantada (`mvn spring-boot:run`). En el laboratorio los puertos
8080 y 8081 estaban ocupados por otros procesos de la máquina, así que se usó el 8099:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8099
```

| Petición | Resultado |
|---|---|
| `GET /productos` (sin datos) | `200` `[]` |
| `POST /productos` `{"nombre":"Laptop","precio":3500.00,"stock":10}` | `201` `{"id":1,"nombre":"Laptop","precio":3500.0,"stock":10}` |
| `POST /productos` `{"nombre":"Mouse","precio":80.00,"stock":20}` | `201` `{"id":2,...}` |
| `GET /productos` | `200` con los dos productos |
| `GET /productos/1` | `200` `{"id":1,"nombre":"Laptop",...}` |
| `GET /productos/99` | `404` |
| `POST` con `"precio":0` | `400` `{"mensaje":"El precio debe ser mayor que cero"}` |
| `POST` con `"nombre":""` | `400` `{"mensaje":"El nombre es obligatorio"}` |
| `PUT /productos/1` `{"nombre":"Laptop Pro","precio":4200.00,"stock":7}` | `200` `{"id":1,"nombre":"Laptop Pro","precio":4200.0,"stock":7}` |
| `PUT /productos/99` | `404` |
| `DELETE /productos/2` | `204` |
| `DELETE /productos/2` (repetido) | `404` |
| `POST` sin cabecera `Content-Type` | `415 Unsupported Media Type` |

El último caso es el error frecuente que menciona la sección 11 de la guía: sin
`Content-Type: application/json`, Spring rechaza el cuerpo con 415.

## Ejercicios complementarios de la guía

- **Ejercicio 1** (buscar producto existente): `buscarPorIdCuandoExiste_debeRetornar200ConElProducto`
  en el controlador y `buscarPorIdCuandoExiste_debeRetornarElProducto` en el servicio.
- **Ejercicio 2** (eliminar): `ProductoService.eliminar(Long id)` devuelve `boolean`, y
  `DELETE /productos/{id}` responde 204 o 404.
- **Ejercicio 3** (actualizar): `ProductoService.actualizar(Long id, Producto datos)` devuelve
  `Optional`, no actualiza un producto inexistente y revalida precio, nombre y stock.

## Preguntas de reflexión

**1. ¿Por qué TDD no es lo mismo que escribir pruebas después de terminar el código?**
Porque cambia quién manda. En TDD la prueba define el comportamiento y el diseño se acomoda a ella;
al escribirla después, la prueba se acomoda al código que ya existe y termina confirmando lo que el
código hace, incluidos sus errores.

**2. ¿Qué representa la fase RED?**
La prueba que falla es la evidencia de que la funcionalidad todavía no existe y de que la prueba
realmente la está midiendo. Una prueba que nunca se vio fallar no demuestra nada: podría estar
pasando siempre.

**3. ¿Por qué no debe saltarse la fase REFACTOR?**
Porque GREEN premia el código mínimo, no el código bueno. Sin refactor, la deuda se acumula. En el
ciclo 2 los tres `if` dentro de `registrar()` funcionaban, pero mezclaban validación con registro;
extraerlos a `validar()` fue seguro precisamente porque las pruebas ya protegían el comportamiento.

**4. ¿Qué diferencia hay entre probar ProductoService y ProductoController?**
`ProductoServiceTest` es una prueba unitaria pura: instancia la clase con `new`, no levanta Spring y
verifica reglas de negocio. `ProductoControllerTest` levanta la porción web con `@WebMvcTest` y
verifica códigos HTTP, rutas y JSON, no las reglas.

**5. ¿Para qué sirve MockMvc?**
Permite ejecutar peticiones HTTP contra el controlador sin levantar un servidor ni abrir un puerto:
las pruebas son rápidas y deterministas, y aun así pasan por el mapeo de rutas y la serialización JSON.

**6. ¿Por qué en @WebMvcTest se usa un mock del servicio?**
Para aislar la capa web. Si el controlador usara el servicio real, un fallo podría venir de cualquiera
de los dos. Con `@MockitoBean` se fija la respuesta del servicio y cualquier fallo apunta al controlador.

**7. ¿Qué error se genera cuando Angular o Postman envía un JSON con tipos incorrectos?**
`400 Bad Request`: Jackson no puede deserializar el cuerpo y lanza `HttpMessageNotReadableException`.
Distinto es olvidar la cabecera `Content-Type: application/json`, que produce `415 Unsupported Media Type`.

**8. ¿Cómo ayuda TDD a mejorar el diseño de una API REST?**
Obliga a decidir el contrato antes que la implementación: ruta, código HTTP y forma del JSON se eligen
al escribir la prueba. Además empuja a separar capas, porque una clase que hace demasiado es difícil
de probar, y esa dificultad aparece de inmediato.

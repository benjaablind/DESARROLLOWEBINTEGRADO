[PORTADA2]

# 1. Regla que ordena esta exposición

La exposición cubre **únicamente los temas dictados en las semanas 1 a 4** del curso, según la síntesis oficial:

| Semana | Tema | Contenido |
|---|---|---|
| 1 | Fundamentos de Spring Boot | Introducción, arquitectura base, entorno (VS Code, Java 25, Maven), primer proyecto |
| 2 | Endpoints, controladores e inyección de dependencias | Rutas HTTP, `@RestController` y mappings, Controller + Service, DI por constructor |
| 3 | TDD en Spring Boot | Red → Green → Refactor, JUnit 5, Mockito, MockMvc |
| 4 | Implementación de API REST y pruebas | GET, POST, PUT, PATCH, DELETE; `@RequestBody`, `@PathVariable`, `@RequestParam`; `ResponseEntity`; validación y Postman |

Tecnologías que se pueden nombrar en la exposición: Java 25, Spring Boot, Maven, VS Code, JUnit 5, Mockito, MockMvc y Postman.

**Lo que NO se expone**, por corresponder a unidades posteriores: PostgreSQL/MySQL, JPA e Hibernate, JPQL, transacciones, Spring Security, JWT, Angular, SASS y despliegue en la nube. El proyecto tiene avances en algunos de esos frentes, pero se mencionan en una sola frase de cierre y no se desarrollan.

Cada integrante expone **un tema del curso aplicado a su propio módulo**. Así se cumplen las dos exigencias a la vez: la rúbrica evalúa el dominio de lo estudiado, y cada persona responde por el código que escribió.

[SALTO]

# 2. Reparto de la exposición

| Turno | Integrante | Módulo del proyecto | Semana que expone | Tema | Minutos |
|---|---|---|---|---|---|
| 1 | Integrante 1 | Usuarios, roles y autenticación | Semana 1 | Fundamentos de Spring Boot, entorno y arquitectura base | 4 |
| 2 | Integrante 2 | Pacientes | Semana 2 | Endpoints, controladores e inyección de dependencias | 4 |
| 3 | Integrante 3 | Citas, agenda y dashboard | Semana 4 (parte A) | Verbos HTTP, parámetros y `ResponseEntity` | 3 |
| 4 | Integrante 4 | Historia clínica y odontograma | Semana 4 (parte B) | Validación de datos y manejo de errores | 3 |
| 5 | Integrante 5 | Tratamientos y pagos | Semana 3 | Desarrollo guiado por pruebas (TDD) | 4 |
| 6 | Integrante 6 | Archivos clínicos y reportes | Semana 4 (parte C) | Pruebas de la API con Postman y ejecución de la suite | 4 |
| — | — | — | — | Cierre y preguntas | 3 |

**Total: 25 minutos.** Al final de este documento se incluye una versión comprimida a 15 minutos.

[SALTO]

# 3. Guion detallado por integrante

## Turno 1 — Integrante 1: Fundamentos de Spring Boot (Semana 1)

**Módulo del que responde:** usuarios, roles y autenticación. Además, es quien preparó la estructura general del back-end, por lo que le corresponde abrir la exposición.

**Qué explica:**

1. **El problema y el objetivo**, en menos de un minuto: una clínica odontológica que administra pacientes, agenda, historias y pagos en papel; el objetivo es una API RESTful que centralice esa información.
2. **El entorno de trabajo**: VS Code, JDK 25 y Maven. Explica qué aporta cada pieza y por qué el proyecto se generó con Spring Initializr.
3. **Qué es Spring Boot y qué resuelve**: autoconfiguración, *starters* y servidor embebido. La idea que debe quedar clara es que Spring Boot elimina la configuración repetitiva para dejar al desarrollador ocupado del negocio.
4. **La arquitectura base del proyecto**: los siete paquetes —`controller`, `service`, `repository`, `model`, `dto`, `exception`, `config`— y la regla de dependencia en un solo sentido.
5. **El primer proyecto en marcha**: arranca el servidor y muestra el mensaje de inicio.

**Qué muestra en pantalla:**

- `pom.xml`, señalando las cuatro dependencias y **el comentario de alcance**: todavía no hay JPA, base de datos ni Security, porque corresponden a las semanas 6 a 10. Esta es una frase que juega a favor del equipo: demuestra que el alcance se controló a propósito.
- El árbol de paquetes del proyecto.
- La consola con `mvn spring-boot:run` y el arranque en el puerto 8080.
- `POST /api/auth/login` respondiendo, como primera prueba de que el servidor vive.

**Frase de cierre para dar el turno:** «Con el proyecto arrancando, el siguiente paso es exponer recursos. Eso es lo que explica [Integrante 2] con el módulo de pacientes.»

**Preguntas probables:**

- *¿Por qué Spring Boot y no Spring tradicional?* Por la autoconfiguración y el servidor embebido: menos configuración manual y un solo ejecutable.
- *¿Qué es un starter?* Un paquete de dependencias coherentes entre sí; `spring-boot-starter-web` trae Spring MVC, Tomcat embebido y Jackson.
- *¿Por qué no hay base de datos?* Porque corresponde a la unidad de las semanas 6 a 10; los repositorios ya están aislados para que la migración no toque los servicios.

## Turno 2 — Integrante 2: Endpoints, controladores e inyección de dependencias (Semana 2)

**Módulo del que responde:** pacientes. Es el módulo que el propio plan de trabajo señala como primera versión de la API, así que es el ejemplo natural de este tema.

**Qué explica:**

1. **Cómo se define un endpoint**: `@RestController` sobre la clase, `@RequestMapping("/api/pacientes")` para la ruta base y las anotaciones de método para cada verbo.
2. **El diseño de rutas REST**: el recurso en plural, el identificador como segmento de la ruta, los filtros como parámetros de consulta.
3. **La separación Controller + Service**: el controlador traduce HTTP; el servicio decide. Debe decirlo con un ejemplo concreto: «el controlador no sabe que un DNI no puede repetirse; eso lo sabe `PacienteService`».
4. **Inyección de dependencias por constructor**: el campo `final`, el constructor que recibe el servicio y la razón de fondo —la clase no construye a sus colaboradores, los recibe del contenedor—. Debe explicar por qué se prefiere el constructor sobre `@Autowired` en el campo: dependencias explícitas, objeto siempre completo y clase fácil de probar.

**Qué muestra en pantalla:**

- `PacienteController.java` completo: es corto y se lee de un vistazo.
- `PacienteService.java`, señalando dónde vive la regla del DNI repetido.
- `GET /api/pacientes?texto=torres` ejecutado en vivo.

**Frase clave:** «El controlador es una traducción entre HTTP y el dominio: recibe, delega y responde. Ninguna regla de negocio vive ahí.»

**Preguntas probables:**

- *¿Qué diferencia hay entre `@Controller` y `@RestController`?* `@RestController` equivale a `@Controller` más `@ResponseBody`: lo que devuelve el método es el cuerpo de la respuesta, serializado a JSON.
- *¿Por qué inyección por constructor?* Permite declarar la dependencia `final`, evita objetos a medio construir y hace trivial pasar un doble en las pruebas.
- *¿Quién crea el objeto `PacienteService`?* El contenedor de Spring, porque la clase está anotada con `@Service`.

## Turno 3 — Integrante 3: Verbos HTTP, parámetros y ResponseEntity (Semana 4, parte A)

**Módulo del que responde:** citas, agenda y dashboard. Es el módulo con más variedad de verbos y de parámetros, así que ilustra bien este tema.

**Qué explica:**

1. **Los cinco verbos y cuándo usar cada uno**, con ejemplos de su propio módulo: `GET /api/citas` consulta, `POST /api/citas` crea, `PUT /api/citas/{id}` reemplaza, `PATCH /api/citas/{id}/confirmar` modifica solo una parte y `DELETE /api/citas/{id}` elimina.
2. **Las tres formas de recibir datos**: `@PathVariable` para `/api/citas/{id}`, `@RequestParam` para `/api/citas?dia=&estado=` y `@RequestBody` para el JSON de creación.
3. **`ResponseEntity` y los códigos de estado**: `200` en consultas, `201` con la cabecera `Location` al crear, `204` al eliminar. Debe explicar por qué se devuelve `Location`: el cliente necesita saber dónde quedó el recurso recién creado.
4. **Un endpoint de solo lectura que compone información**: `GET /api/dashboard` reúne indicadores de varios módulos en un único DTO de respuesta.

**Qué muestra en pantalla:**

- `CitaController.java`, recorriendo los verbos.
- Postman: `POST /api/citas` devolviendo `201` con la cabecera `Location` visible en la pestaña de cabeceras.
- `GET /api/citas/agenda?dia=...` con la agenda del día.

**Frase clave:** «El verbo y el código de estado son parte del contrato: dicen qué se hizo y cómo terminó, sin que el cliente tenga que leer el mensaje.»

**Preguntas probables:**

- *¿Diferencia entre PUT y PATCH?* `PUT` reemplaza el recurso completo; `PATCH` modifica solo una parte, como el estado de la cita.
- *¿Por qué `201` y no `200` al crear?* Porque `201 Created` indica que se creó un recurso nuevo, y se acompaña de `Location` con su URI.
- *¿`@RequestParam` o `@PathVariable`?* La ruta identifica al recurso; el parámetro de consulta filtra u ordena.

## Turno 4 — Integrante 4: Validación de datos y manejo de errores (Semana 4, parte B)

**Módulo del que responde:** historia clínica y odontograma. Es el módulo con las validaciones de dominio más llamativas —la notación FDI de las piezas dentales—, así que el ejemplo se explica solo.

**Qué explica:**

1. **Validación declarativa en el DTO**: `@NotBlank`, `@NotNull`, `@Pattern`, `@Past`, activadas con `@Valid` en el parámetro del controlador. La idea a transmitir: la validación se declara, no se programa con `if`.
2. **Dos niveles de validación distintos**: el formato se valida en el DTO; las reglas que necesitan consultar el estado del sistema —que el número de pieza sea válido en la notación FDI, que la historia exista— viven en el servicio.
3. **Manejo centralizado de errores**: `@RestControllerAdvice` en `ApiExceptionHandler`. Debe señalar que **ningún controlador tiene `try/catch`** y que toda la API devuelve la misma estructura de error: estado, mensaje, ruta y fecha.
4. **Una decisión de diseño propia**: el módulo de historia clínica **no expone `PUT` ni `DELETE` a propósito**. La información clínica no se sobrescribe ni se borra; se anula indicando el motivo y permanece en el historial. El mismo criterio rige en el odontograma, donde cada cambio crea un registro nuevo. Este punto suele ser muy bien recibido, porque muestra criterio y no solo ejecución.

**Qué muestra en pantalla:**

- Un DTO con sus anotaciones de validación.
- `ApiExceptionHandler.java`.
- Postman: `POST /api/odontograma` con `numeroPieza: 99` devolviendo `400` con el mensaje «El numero de pieza debe estar entre 11 y 85», y un `GET` a un identificador inexistente devolviendo `404`.

**Frase clave:** «Un error también es una respuesta de la API, y como tal tiene un formato acordado. Por eso está en un solo lugar.»

**Preguntas probables:**

- *¿Qué hace `@Valid`?* Dispara la validación de las anotaciones del DTO antes de que el método del controlador se ejecute.
- *¿Por qué un manejador global?* Para no repetir `try/catch` en once controladores y para garantizar la misma estructura de error en toda la API.
- *¿Por qué no se puede editar una historia clínica?* Porque es un registro clínico: se anula con su motivo, pero no se sobrescribe.

## Turno 5 — Integrante 5: Desarrollo guiado por pruebas (Semana 3)

**Módulo del que responde:** tratamientos y pagos. La regla «un pago no puede superar el saldo pendiente» es el ejemplo ideal para mostrar el ciclo completo del TDD.

**Qué explica:**

1. **El ciclo Red → Green → Refactor**, dicho con sus propias palabras: primero la prueba que falla, luego el código mínimo que la hace pasar, después la mejora del diseño con la prueba como red de seguridad.
2. **Las herramientas y para qué sirve cada una**: JUnit 5 ejecuta las pruebas, Mockito sustituye colaboradores por dobles, y MockMvc envía solicitudes HTTP simuladas contra los controladores reales sin levantar un servidor.
3. **El ciclo aplicado a una regla concreta**, recorriéndolo en vivo sobre `PagoControllerTest`:
   - **Red:** la prueba «un pago mayor al saldo devuelve 400» falla, porque el servicio todavía acepta cualquier monto.
   - **Green:** se agrega la comprobación del saldo en `PagoService` y la prueba pasa.
   - **Refactor:** el cálculo del saldo se extrae a un método propio, reutilizado por el estado de cuenta; las pruebas siguen verdes.
4. **Por qué las pruebas se aíslan entre sí**: el `@BeforeEach` limpia el repositorio y vuelve a cargar los mismos datos, de modo que ninguna prueba depende de otra ni del orden de ejecución.

**Qué muestra en pantalla:**

- `PagoControllerTest.java`, señalando la estructura de una prueba: preparar, ejecutar, verificar.
- La ejecución de esa clase de prueba en verde.
- Opcionalmente, comentando la línea de la validación en `PagoService`, la prueba en rojo, y al restaurarla, en verde otra vez. Es la demostración más convincente del tema.

**Frase clave:** «La prueba se escribe antes porque obliga a decidir qué debe pasar antes de decidir cómo hacerlo.»

**Preguntas probables:**

- *¿Qué es MockMvc?* Un componente que simula solicitudes HTTP contra los controladores con todo el contexto de Spring activo —validaciones, conversión JSON y manejador de excepciones incluidos— sin abrir un puerto.
- *¿Cuándo Mockito y cuándo MockMvc?* Mockito para aislar una clase de sus colaboradores en una prueba unitaria; MockMvc para verificar el comportamiento del endpoint de extremo a extremo dentro de la aplicación.
- *¿El TDD no hace más lento el desarrollo?* Al inicio sí; a cambio, cada regla de negocio queda verificada y el sistema se puede refactorizar sin miedo.

## Turno 6 — Integrante 6: Pruebas de la API y resultados (Semana 4, parte C)

**Módulo del que responde:** archivos clínicos y reportes. Su módulo aporta el caso de solicitud que no es JSON —la subida `multipart`— y los reportes con rangos de fechas, los dos casos más ilustrativos para cerrar con pruebas.

**Qué explica:**

1. **Cómo se prueba una API con Postman**: colección organizada por módulo, cuerpo de la solicitud, verificación del código de estado y del contenido de la respuesta.
2. **Casos correctos y casos de error**, sobre su propio módulo: subir un archivo permitido frente a uno con extensión no admitida o mayor a diez megabytes; un reporte con rango válido frente a uno con la fecha inicial posterior a la final, que devuelve `400`.
3. **El resultado de la suite automatizada**: ejecuta `mvn test` en vivo y muestra el resumen final —**156 pruebas, 0 fallos, 0 errores**—. Es el cierre más fuerte posible de la exposición.
4. **Cierre del equipo, en tres frases**: lo logrado en estas cuatro semanas (una API REST completa, por capas, probada); lo que sigue según el curso (base de datos con JPA, seguridad con JWT, Angular y despliegue); y la mención en una sola frase de que el equipo ya tiene un avance del front-end, sin desarrollarlo.

**Qué muestra en pantalla:**

- Postman: subida de un archivo con `multipart/form-data` y el rechazo de una extensión no permitida.
- `GET /api/reportes/ingresos?desde=&hasta=` con el reporte agrupado por método de pago y por mes.
- La consola con el resumen de `mvn test`.

**Frase de cierre:** «En estas cuatro semanas construimos la API completa del sistema, con 69 endpoints y 156 pruebas en verde. La base de datos, la seguridad y el despliegue entran en las siguientes unidades, y el proyecto ya está estructurado para recibirlas.»

**Preguntas probables:**

- *¿Cómo se prueba una subida de archivo?* Con `multipart/form-data`, enviando el archivo y sus metadatos como partes de la solicitud.
- *¿Qué pasa si el servidor se reinicia?* Los datos se pierden: el almacenamiento es en memoria en esta etapa, por alcance del curso.
- *¿Estas pruebas son unitarias o de integración?* De integración: levantan el contexto de Spring y ejercitan el endpoint completo. Las unitarias con Mockito están planteadas como mejora.

[SALTO]

# 4. Qué hacer si preguntan por temas de semanas posteriores

Puede ocurrir que el docente pregunte por base de datos, seguridad o Angular. La respuesta correcta no es improvisar, sino **responder con el alcance y con la preparación ya hecha**:

| Si preguntan por… | Respuesta breve |
|---|---|
| Base de datos, JPA, Hibernate | «Corresponde a las semanas 6 a 10. El modelo de dominio ya está definido en el paquete `model` y todos los repositorios extienden una clase base que expone las mismas operaciones que `JpaRepository`, de modo que la migración no obligará a modificar los servicios.» |
| Spring Security y JWT | «Corresponde a las semanas 6 a 10. Hoy el inicio de sesión compara credenciales en el servicio y no hay protección real de endpoints; el paquete `security` ya está reservado y el punto único de autenticación permite añadir el filtro sin tocar los controladores.» |
| Angular | «Corresponde a las semanas 11 a 15. El equipo tiene un avance funcional, pero no forma parte de esta exposición.» |
| Despliegue y CI/CD | «Corresponde a las semanas 16 a 18. Está planificado con empaquetado JAR, Nginx como proxy inverso y GitHub Actions ejecutando la suite de pruebas ante cada Pull Request.» |

Decir con naturalidad «eso corresponde a la unidad siguiente y así lo dejamos preparado» demuestra control del proyecto. Improvisar sobre un tema no estudiado produce el efecto contrario.

[SALTO]

# 5. Versión comprimida a 15 minutos

Si el tiempo asignado es menor, se mantiene el mismo reparto reduciendo el contenido, nunca el número de expositores:

| Turno | Integrante | Contenido reducido | Minutos |
|---|---|---|---|
| 1 | Integrante 1 | Problema, objetivo, Spring Boot y arquitectura por capas. Sin recorrer el `pom.xml` línea por línea | 2,5 |
| 2 | Integrante 2 | `@RestController`, Controller + Service e inyección por constructor, con un solo endpoint como ejemplo | 2,5 |
| 3 | Integrante 3 | Los cinco verbos y `201` con `Location`, con una sola demostración en Postman | 2 |
| 4 | Integrante 4 | `@Valid` y el manejador global, con un único error mostrado en vivo | 2 |
| 5 | Integrante 5 | El ciclo Red–Green–Refactor sobre la regla del saldo | 3 |
| 6 | Integrante 6 | `mvn test` con las 156 pruebas y cierre | 3 |

[SALTO]

# 6. Lista de verificación antes de exponer

**Preparación técnica (una hora antes):**

- Back-end arrancado y respondiendo en `http://localhost:8080/api`, con los datos de demostración cargados.
- Colección de Postman abierta, con las solicitudes de la exposición ya preparadas y en el orden en que se usarán.
- Editor con los archivos que se van a mostrar ya abiertos en pestañas, en el orden de los turnos.
- Tamaño de letra del editor y de la terminal aumentado para que se lea desde el fondo del aula.
- `mvn test` ejecutado una vez antes de la exposición, para que Maven tenga las dependencias en caché y la demostración no dependa de la red.
- Una captura de respaldo de cada demostración, por si falla la conexión o el servidor.

**Acuerdos de equipo:**

- Cada integrante expone su turno de pie y cede el turno nombrando al siguiente y al tema que sigue.
- Nadie interrumpe el turno de otro; las precisiones se hacen al final, en el bloque de preguntas.
- Las preguntas sobre un módulo las responde el integrante responsable de ese módulo.
- Ensayar completo al menos una vez con cronómetro. El error más frecuente es que los dos primeros turnos consuman el tiempo de los últimos.

**Qué debe quedar demostrado al terminar:**

1. Que el equipo sabe qué es Spring Boot y por qué se eligió.
2. Que sabe construir un endpoint REST y separar el controlador del servicio.
3. Que entiende y aplica la inyección de dependencias por constructor.
4. Que aplica el ciclo del TDD y puede mostrarlo en vivo.
5. Que la API usa correctamente verbos, parámetros y códigos de estado, y valida sus entradas.
6. Que las pruebas existen, se ejecutan y pasan.

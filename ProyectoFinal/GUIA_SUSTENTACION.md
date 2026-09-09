# Guía de sustentación — Sistema de Gestión Odontológica

Lo que pide el profesor para esta entrega:

- **Un solo entregable:** el enlace al repositorio **público** en GitHub.
- **Sin informe, sin Word, sin diapositivas.**
- **Sustentación en vivo:** la aplicación corriendo, mostrando su funcionamiento —principalmente con Postman— y el código de cómo lo hicieron.
- La nota del grupo sale de esa sustentación.

Se expone únicamente lo estudiado en las **semanas 1 a 4**: fundamentos de Spring Boot, endpoints y controladores, inyección de dependencias, TDD y API REST con validación y pruebas. Base de datos, seguridad con JWT, Angular y despliegue corresponden a unidades posteriores.

---

## 1. Antes de la sustentación (hacerlo hoy, no mañana)

### 1.1. Estado de la entrega

Estas dos condiciones ya están cumplidas; conviene volver a comprobarlas la mañana de la sustentación:

- **El repositorio es público.** Abrir `https://github.com/benjaablind/DESARROLLOWEBINTEGRADO` en una ventana de incógnito, sin sesión iniciada, y confirmar que carga.
- **`main` tiene el proyecto completo.** Los seis módulos entraron por Pull Request, uno por integrante. En la raíz de `main` deben verse `ProyectoFinal/backend` y `ProyectoFinal/frontend`, y en `controller/` los once controladores.

### 1.2. Java y Maven: comprobarlo antes, no en el aula

El proyecto usa **Java 25**, declarado en el `pom.xml`. Con un JDK anterior la compilación falla con `release version 25 not supported`, y ese error delante del profesor cuesta caro.

```bash
java -version    # debe decir 25
```

Si dice 24 o menos, hay dos salidas:

- **Recomendada:** instalar el **JDK 25** (Eclipse Temurin o el de Oracle) en la máquina que va a proyectar. Es lo que dice el material del curso y deja los comandos limpios.
- **Rápida, para hoy:** añadir `-Djava.version=24` a cada comando, ajustando el número al JDK que tengan.

**Maven no hace falta instalarlo.** El proyecto trae el wrapper: `mvnw` en Git Bash y `mvnw.cmd` en PowerShell o CMD. Si escriben `mvn` a secas y no lo tienen instalado, sale «El término 'mvn' no se reconoce».

| Terminal | Arrancar el backend | Ejecutar las pruebas |
|---|---|---|
| PowerShell, con JDK 25 | `.\mvnw.cmd spring-boot:run` | `.\mvnw.cmd test` |
| PowerShell, con JDK 24 | `.\mvnw.cmd "-Djava.version=24" spring-boot:run` | `.\mvnw.cmd "-Djava.version=24" test` |
| Git Bash, con JDK 25 | `./mvnw spring-boot:run` | `./mvnw test` |

En PowerShell **las comillas alrededor de `-Djava.version=24` son obligatorias**: sin ellas, PowerShell parte el argumento y Maven responde `Unknown lifecycle phase ".version=24"`.

### 1.3. Preparación de la máquina que va a proyectar

1. Clonar el repositorio limpio en una carpeta nueva y verificar que arranca. Si funciona recién clonado, funciona en cualquier equipo.
2. Levantar el backend y dejarlo corriendo, desde `ProyectoFinal/backend` y con el comando que corresponda a su terminal según la tabla de arriba. Queda en `http://localhost:8080/api` con los datos de demostración cargados.
3. Ejecutar las pruebas una vez, para que Maven deje las dependencias en caché y la demostración no dependa de la red del aula.
4. Importar la colección de Postman (sección 2).
5. Abrir en el editor, en pestañas y en este orden, los archivos que se van a mostrar:
   - `pom.xml`
   - `controller/PacienteController.java`
   - `service/PacienteService.java`
   - `dto/PacienteRequest.java`
   - `exception/ApiExceptionHandler.java`
   - `test/java/com/utp/odontologia/PagoControllerTest.java`
6. Aumentar el tamaño de letra del editor y de la terminal para que se lea desde el fondo del aula.
7. Tener una segunda máquina lista con todo lo anterior, por si la primera falla.

### 1.4. Qué llevar preparado

- El enlace del repositorio escrito, listo para pegar en el Classroom.
- Postman con la colección importada y probada **antes** de entrar al aula.
- La terminal con el backend ya arrancado: nadie quiere ver treinta segundos de compilación en silencio.

---

## 2. Postman: qué es y cómo usarlo

### 2.1. Por qué el profesor lo pide

Postman es un cliente HTTP: permite enviar solicitudes a la API y ver la respuesta sin necesidad de un navegador ni de una interfaz. Es la herramienta que demuestra que **el back-end funciona por sí solo**, que es exactamente lo que se estudió en las semanas 1 a 4. Con Postman se ve en pantalla lo que en clase se explicó en teoría: el verbo HTTP, la ruta del recurso, el cuerpo JSON, el código de estado y la respuesta.

### 2.2. Instalación e importación

1. Descargar Postman de `https://www.postman.com/downloads/` e instalarlo. Si no quieren crear cuenta, la aplicación de escritorio permite trabajar sin iniciar sesión. Alternativa válida: la extensión **Thunder Client** de VS Code, que el plan de trabajo también contempla.
2. Abrir Postman y pulsar **Import** (arriba a la izquierda).
3. Seleccionar el archivo `ProyectoFinal/postman/Sistema_Odontologico.postman_collection.json` del repositorio.
4. Aparece la colección **Sistema de Gestión Odontológica – API REST** con seis carpetas, una por integrante.

No hay que configurar nada más: la dirección del servidor está en la variable `baseUrl` de la colección y las fechas y el DNI de prueba se calculan solos en cada ejecución.

### 2.3. Cómo enviar una solicitud y qué mirar en pantalla

Al abrir cualquier solicitud y pulsar **Send**, hay cuatro cosas que señalar:

| Dónde mirar | Qué se ve | Qué decir |
|---|---|---|
| Arriba, junto a la URL | El **verbo** (GET, POST, PUT, PATCH, DELETE) y la ruta del recurso | «El verbo dice qué operación es; la ruta identifica el recurso» |
| Pestaña **Body** de la solicitud | El JSON que se envía | «Esto llega al controlador como `@RequestBody` y se valida antes de entrar» |
| Barra de la respuesta | **Status** (200, 201, 400, 404, 204), tiempo y tamaño | «El código de estado es parte del contrato de la API» |
| Pestaña **Headers** de la respuesta | La cabecera **Location** en las creaciones | «Al crear devolvemos 201 y la URI donde quedó el recurso» |

Además, cada solicitud de la colección trae una **prueba automática** que verifica el código de estado esperado. Al enviar, la pestaña **Test Results** muestra `PASS`. Es un detalle que juega a favor: no solo se envía la solicitud, se comprueba que respondió lo que debía.

### 2.4. Ejecutar toda la colección de una vez

Clic derecho sobre la colección → **Run collection** → **Run**. Postman ejecuta las 65 solicitudes en orden y muestra el resumen con todas las pruebas en verde. Es el cierre más fuerte de la demostración, y dura menos de un minuto.

Las carpetas están pensadas para eso: cada una crea sus datos, prueba el caso correcto, prueba el caso de error y limpia lo que creó, así que la colección se puede ejecutar cuantas veces haga falta.

### 2.5. La solicitud de subida de archivo

**«Subir un archivo clínico»**, en la carpeta 6, es la única que envía `multipart/form-data` en lugar de JSON. Ya viene apuntando a `radiografia-ejemplo.png`, que está en la carpeta `postman/` del repositorio, así que normalmente funciona sin tocar nada.

Si Postman avisa que no encuentra el archivo, hay dos salidas: ir a **Body → form-data**, fila `archivo`, pulsar **Select Files** y elegirlo a mano; o dejarlo resuelto de una vez en **Settings → General → Working directory**, apuntando a `ProyectoFinal/postman`. Conviene comprobarlo antes de entrar al aula.

---

## 3. Guion de la sustentación en vivo

Duración objetivo: **20 a 25 minutos**. Cada integrante muestra su propio módulo y explica el tema del curso que le corresponde.

### Apertura — Integrante 1 (4 min): la aplicación corriendo y de qué está hecha

**Qué muestra:**

1. La terminal con el backend arrancando (`.\mvnw.cmd spring-boot:run`) y el mensaje de inicio en el puerto 8080. Primera frase: «esto es una API REST hecha con Spring Boot, y ya está corriendo».
2. En Postman, `POST /auth/login` con `admin` / `admin123` → **200** con el usuario autenticado. Luego el mismo login con una clave incorrecta → **400** con la estructura de error.
3. El `pom.xml`: las cuatro dependencias y **el comentario de alcance** que dice que JPA, la base de datos y Spring Security entran en las semanas 6 a 10.
4. El árbol de paquetes: `controller`, `service`, `repository`, `model`, `dto`, `exception`, `config`.

**Qué explica:** qué es Spring Boot y qué resuelve (autoconfiguración, *starters*, servidor embebido); el entorno de trabajo (VS Code, JDK 25, Maven); y la arquitectura por capas con la regla de dependencia en un solo sentido.

**Cede el turno:** «con el servidor corriendo, veamos cómo se construye un recurso completo».

### Turno 2 — Integrante 2 (4 min): un CRUD completo y cómo está hecho por dentro

Es el turno más importante: recorre los cinco verbos sobre un solo recurso.

**Qué muestra, en este orden exacto (carpeta 2 de Postman):**

1. `GET /pacientes` → lista.
2. `GET /pacientes?texto=torres&estado=ACTIVO` → los mismos datos, filtrados. «Esto es `@RequestParam`».
3. `GET /pacientes/1` → la ficha. «Esto es `@PathVariable`».
4. `GET /pacientes/999` → **404**. «El recurso no existe y la API lo dice con el código correcto».
5. `POST /pacientes` → **201**. Abrir la pestaña **Headers** de la respuesta y señalar **Location**.
6. `POST` con `"dni": "123"` → **400**: «falló la validación del formato, ni siquiera entró al controlador».
7. `POST` con el mismo DNI recién creado → **400**: «el formato era correcto, pero el servicio detectó que ya existe. Son dos validaciones distintas».
8. `PUT` (reemplaza todo) y `PATCH /antecedentes` (modifica una parte). «Esa es la diferencia entre PUT y PATCH».
9. `DELETE` → **204**, sin cuerpo.

**Luego pasa al código** y muestra tres archivos, un minuto en total:

- `PacienteController.java`: «el controlador solo traduce HTTP: recibe, delega y responde. Fíjense que no hay ninguna regla de negocio aquí».
- El constructor del controlador: «así se inyecta la dependencia, por constructor, con el campo `final`. El controlador no crea el servicio, lo recibe de Spring».
- `PacienteService.java`: «aquí sí está la regla del DNI repetido».

### Turno 3 — Integrante 3 (3 min): reglas de negocio en la agenda

**Qué muestra (carpeta 3):**

1. `GET /citas/agenda?dia=hoy` → la agenda del día.
2. `POST /citas` para mañana a las 15:00 → **201**.
3. `POST /citas` para el **mismo odontólogo a la misma hora** → **400**: «esta es la regla central del módulo: un odontólogo no puede tener dos citas solapadas».
4. `POST /citas` con fecha del 2020 → **400** por la validación `@Future`.
5. `PATCH /citas/{id}/confirmar` y `POST /citas/{id}/reprogramar` → «reprogramar crea una cita nueva vinculada a la original; la anterior queda marcada, no se borra».
6. `GET /dashboard` → «un solo GET que compone información de todos los módulos en un DTO de respuesta».

**Qué explica:** los cinco verbos, cuándo usar PATCH sobre un subrecurso y por qué el código de estado comunica el resultado sin que el cliente tenga que leer el mensaje.

### Turno 4 — Integrante 4 (3 min): validación y manejo de errores

**Qué muestra (carpeta 4):**

1. `POST /odontograma` con `"numeroPieza": 99` → **400** con el mensaje «El numero de pieza debe estar entre 11 y 85». «Las piezas dentales siguen la notación FDI; el 99 no existe».
2. `POST /historias` sin diagnóstico → **400**: «el DTO declara `@NotBlank`; la validación es declarativa, no la programamos con `if`».
3. `POST /historias` correcto → **201**, y después `PATCH /historias/{id}/anular` con su motivo.
4. `GET /historias/paciente/1` → **la consulta anulada sigue apareciendo**. «Este módulo no tiene PUT ni DELETE a propósito: la información clínica no se sobrescribe ni se borra, se anula indicando el motivo».

**Luego el código:** `PacienteRequest.java` con sus anotaciones de validación, y `ApiExceptionHandler.java`: «ningún controlador tiene `try/catch`. Todos los errores de la API se manejan aquí y por eso todos tienen la misma estructura: estado, mensaje, ruta y fecha».

### Turno 5 — Integrante 5 (4 min): TDD en vivo

Es el turno que más valora la rúbrica del curso, porque el TDD fue el tema de la semana 3.

**Qué muestra (carpeta 5 de Postman y el código):**

1. `POST /tratamientos` → **201**, y `POST /pagos` con un pago parcial de 120 → **201**.
2. `GET /pagos/estado-cuenta/1` → el saldo bajó. «El saldo se calcula, no se guarda».
3. `POST /pagos` con 99999 → **400**: «esta es la regla que vamos a ver escrita como prueba».
4. `DELETE /tratamientos/{id}` con pagos registrados → **400**.

**Y ahora lo importante, en el editor:**

5. Abre `PagoControllerTest.java` y muestra la prueba de esa regla: «esta prueba se escribió **antes** que el código».
6. Explica el ciclo con sus palabras: **Red** (la prueba falla porque el servicio aún acepta cualquier monto) → **Green** (se agrega la comprobación del saldo en `PagoService` y pasa) → **Refactor** (el cálculo del saldo se extrae a un método reutilizado por el estado de cuenta, con las pruebas como red de seguridad).
7. Si hay confianza y tiempo: comentar la línea de la validación en `PagoService`, ejecutar la prueba y mostrarla **en rojo**; restaurarla y mostrarla **en verde**. Es la demostración más convincente de todo el proyecto.

**Menciona las herramientas:** JUnit 5 ejecuta, Mockito sustituye colaboradores, MockMvc envía solicitudes HTTP simuladas contra los controladores reales sin abrir un puerto.

### Turno 6 — Integrante 6 (4 min): pruebas de toda la API y cierre

**Qué muestra (carpeta 6 y la terminal):**

1. `POST /archivos` con `multipart/form-data`: selecciona una imagen y la sube → **201**. «Es la única solicitud que no viaja en JSON».
2. `GET /archivos/{id}/descargar` → el archivo vuelve. «La ruta física nunca aparece en la respuesta; solo se accede por este endpoint».
3. `GET /reportes/ingresos?desde=&hasta=` → ingresos por método de pago y por mes.
4. `GET /reportes/citas` con las fechas invertidas → **400**.
5. **Run collection**: las 65 solicitudes en verde, en menos de un minuto.
6. En la terminal, las pruebas (`.\mvnw.cmd test`): **156 pruebas, 0 fallos, 0 errores**.

**Cierre, tres frases:**

> «En estas cuatro semanas construimos la API completa del sistema: 69 endpoints en 11 controladores, con arquitectura por capas, validación, manejo centralizado de errores y 156 pruebas automatizadas en verde. La base de datos con JPA, la seguridad con JWT, Angular y el despliegue entran en las siguientes unidades del curso, y el proyecto ya está estructurado para recibirlas. El repositorio está en GitHub, cada integrante trabajó en la rama de su módulo y todo se integró por Pull Request revisado.»

---

## 4. El código que hay que mostrar (y nada más)

No recorran archivo por archivo. Con estos cinco, mostrados en el momento en que se explican, alcanza:

| Archivo | Quién lo muestra | Qué demuestra |
|---|---|---|
| `pom.xml` | Integrante 1 | Dependencias y control del alcance |
| `PacienteController.java` | Integrante 2 | `@RestController`, mappings e inyección por constructor |
| `PacienteService.java` | Integrante 2 | Las reglas de negocio fuera del controlador |
| `ApiExceptionHandler.java` | Integrante 4 | Manejo centralizado de errores |
| `PagoControllerTest.java` | Integrante 5 | TDD con JUnit 5 y MockMvc |

Si el profesor pide ver otro módulo, lo abre el integrante responsable de ese módulo.

---

## 5. Si preguntan por temas de semanas posteriores

| Si preguntan por… | Respuesta |
|---|---|
| Base de datos, JPA, Hibernate | «Corresponde a las semanas 6 a 10. El modelo de dominio ya está en el paquete `model` y todos los repositorios extienden una clase base que expone las mismas operaciones que `JpaRepository`, así que la migración no obligará a tocar los servicios.» |
| Spring Security y JWT | «Corresponde a las semanas 6 a 10. Hoy el login compara credenciales en el servicio y los endpoints no están protegidos; el paquete `security` ya está reservado y hay un solo punto de autenticación para añadir el filtro sin tocar los controladores.» |
| Angular | «Corresponde a las semanas 11 a 15. Tenemos un avance funcional en el repositorio, pero hoy sustentamos el back-end.» |
| Despliegue y CI/CD | «Corresponde a las semanas 16 a 18. Está planificado con empaquetado JAR, Nginx como proxy inverso y GitHub Actions ejecutando las pruebas en cada Pull Request.» |
| Por qué los datos se pierden al reiniciar | «Porque la persistencia es en memoria por alcance del curso. Es una decisión, no un olvido: está escrita en el `pom.xml` y en el README.» |

Decir «eso corresponde a la unidad siguiente y así lo dejamos preparado» demuestra control del proyecto. Improvisar sobre un tema no estudiado produce el efecto contrario.

---

## 6. Preguntas probables del profesor

- **¿Qué diferencia hay entre `@Controller` y `@RestController`?** `@RestController` es `@Controller` más `@ResponseBody`: lo que devuelve el método es el cuerpo de la respuesta, serializado a JSON.
- **¿Por qué inyección por constructor y no `@Autowired` en el campo?** Permite declarar la dependencia `final`, deja explícito de qué depende la clase, evita objetos a medio construir y facilita pasar un doble en las pruebas.
- **¿Quién crea los objetos `Service` y `Repository`?** El contenedor de Spring, porque están anotados con `@Service` y `@Repository`.
- **¿Diferencia entre PUT y PATCH?** `PUT` reemplaza el recurso completo; `PATCH` modifica solo una parte.
- **¿Por qué `201` y no `200` al crear?** Porque `201 Created` indica que se creó un recurso nuevo, y se acompaña de la cabecera `Location` con su URI.
- **¿Qué hace `@Valid`?** Dispara la validación de las anotaciones del DTO antes de que el método del controlador se ejecute.
- **¿Qué es MockMvc y en qué se diferencia de Mockito?** MockMvc simula solicitudes HTTP contra los controladores con todo el contexto de Spring activo, sin abrir un puerto; Mockito sustituye colaboradores por dobles para aislar una clase.
- **¿Estas pruebas son unitarias o de integración?** De integración: levantan el contexto de Spring y ejercitan el endpoint completo. Las unitarias puras con Mockito están planteadas como siguiente paso.
- **¿Qué pasa si dos usuarios crean citas a la vez?** El repositorio en memoria usa `ConcurrentHashMap` y `AtomicLong` justamente porque los beans son singleton y varias solicitudes pueden ejecutarse en paralelo.

---

## 7. Acuerdos de equipo y plan B

**Acuerdos:**

- Cada integrante expone su módulo y cede el turno nombrando al siguiente.
- Nadie interrumpe el turno de otro; las precisiones van al final.
- Las preguntas sobre un módulo las responde su responsable.
- Ensayar completo **una vez con cronómetro**. El error más común es que los dos primeros turnos se coman el tiempo de los últimos.

**Plan B, por si algo falla:**

| Si falla… | Qué hacer |
|---|---|
| El proyector o la máquina principal | Segunda laptop con todo instalado y probado |
| El backend no arranca en el aula | Tenerlo ya arrancado desde antes de entrar y **no cerrar la terminal** |
| Postman pide iniciar sesión | Usar Thunder Client en VS Code, o los comandos `curl` de `backend/DOCUMENTACION_API.md` |
| No hay internet | Todo es local: backend en `localhost:8080` y Postman de escritorio. Haber ejecutado las pruebas antes deja las dependencias en caché |
| Una solicitud devuelve algo inesperado | Reiniciar el backend: recarga los datos de demostración y deja el sistema en su estado inicial |

**Lo que debe quedar demostrado al terminar:**

1. Que la aplicación corre y responde.
2. Que el equipo sabe construir un endpoint REST y separar el controlador del servicio.
3. Que entiende y aplica la inyección de dependencias por constructor.
4. Que valida las entradas y maneja los errores en un solo lugar.
5. Que aplica TDD y puede mostrarlo en vivo.
6. Que las pruebas existen, se ejecutan y pasan.

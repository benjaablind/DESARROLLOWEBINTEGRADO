# Flujo de la exposición, integrante por integrante

Cada uno expone **su módulo** siguiendo siempre los mismos cuatro pasos:

```
1. Su commit  →  2. Su código  →  3. Su prueba  →  4. Su Postman
```

Ese orden cuenta una historia: «esto lo hice yo, así está construido, así lo verifiqué, y así funciona en vivo». Repetirlo seis veces le da ritmo a la sustentación y deja claro que cada persona responde por lo suyo.

## Reparto

| Turno | Integrante | Módulo | Su commit | Clase de prueba | Carpeta en Postman |
|---|---|---|---|---|---|
| 1 | yeffdx | Usuarios y autenticación | `a0a0692` | `UsuarioControllerTest` (22) | 1 |
| 2 | Angelo | Pacientes | `c31c0c7` | `PacienteControllerTest` (21) | 2 |
| 3 | Benjamin | Citas, agenda y dashboard | `b47f9a3` | `CitaControllerTest` (20) | 3 |
| 4 | Jeaneli | Historia clínica y odontograma | `d5ff041` | `HistoriaClinicaControllerTest` (14) | 4 |
| 5 | Leonardo | Tratamientos y pagos | `7aab87d` | `PagoControllerTest` (13) | 5 |
| 6 | Marco | Archivos clínicos y reportes | `cc80d20` | `ArchivoControllerTest` (14) | 6 |

**Antes de empezar:** backend corriendo, Postman con la colección importada, y en el navegador la pestaña del repositorio en GitHub abierta en la lista de commits.

Comando para ejecutar solo la clase de pruebas de tu módulo, desde `ProyectoFinal\backend`:

```powershell
.\mvnw.cmd test "-Dtest=NombreDeTuClaseTest"
```

---

# Turno 1 — Usuarios y autenticación

**Tema del curso que le toca explicar:** Semana 1, fundamentos de Spring Boot y arquitectura por capas.

Abre la sustentación, así que además presenta el proyecto en dos frases.

### 1. Su commit

`a0a0692 feat: modulo de usuarios, roles y autenticacion`

En GitHub, pestaña **Commits**, señala el suyo: «este es mi módulo, entró por Pull Request y lo revisó otro compañero».

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `pom.xml` | Las cuatro dependencias y el comentario de alcance: JPA y Security entran en las semanas 6 a 10 |
| Árbol de paquetes | `controller`, `service`, `repository`, `model`, `dto`, `exception`, `config` |
| `UsuarioController.java` | `@RestController`, `@RequestMapping("/api/usuarios")` |
| `UsuarioResponse.java` | **No tiene campo `password`**, aunque la entidad sí lo tiene |

### 3. Su prueba

```powershell
.\mvnw.cmd test "-Dtest=UsuarioControllerTest"
```

La prueba que vale la pena leer en voz alta:

> `GET /api/usuarios devuelve los usuarios registrados sin la contrasena`

### 4. Su Postman — carpeta 1

| Solicitud | Resultado | Qué decir |
|---|---|---|
| Login correcto | `200` | «Devuelve el usuario, nunca la contraseña» |
| Login con credenciales incorrectas | `400` | «Y este es el formato de error de toda la API» |
| Crear usuario | `201` | Abrir **Headers** y señalar `Location` |
| Crear usuario repetido | `400` | «El nombre de usuario no puede repetirse» |

### 5. Qué decir

> «Buenas, somos el grupo del Sistema de Gestión Odontológica: una API REST con Spring Boot para manejar pacientes, citas, historias clínicas, tratamientos y pagos de una clínica dental.
>
> Yo hice el módulo de usuarios y autenticación, y la estructura base del backend. El proyecto está organizado en siete paquetes, cada uno con una responsabilidad: el controlador traduce HTTP, el servicio tiene las reglas de negocio y el repositorio guarda los datos. Las dependencias van en un solo sentido, y eso es lo que va a permitir cambiar a base de datos en la siguiente unidad sin tocar los servicios.
>
> Aquí en el `pom.xml` dejamos escrito qué no incluimos todavía y por qué: JPA y Spring Security entran en las semanas 6 a 10.»

**Cede el turno:** «con el servidor corriendo, [Angelo] les muestra cómo se construye un recurso completo».

### Preguntas probables

- **¿Por qué Spring Boot?** Autoconfiguración, starters y servidor embebido: menos configuración y un solo ejecutable.
- **¿Qué es un starter?** Un paquete de dependencias compatibles entre sí; `spring-boot-starter-web` trae Spring MVC, Tomcat y Jackson.
- **¿Por qué no hay seguridad todavía?** Corresponde a las semanas 6 a 10. El paquete `security` ya está reservado.

---

# Turno 2 — Pacientes

**Tema:** Semana 2, endpoints, controladores e inyección de dependencias.

Es el turno más importante: recorre los cinco verbos sobre un mismo recurso.

### 1. Su commit

`c31c0c7 feat: modulo de pacientes`

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `PacienteController.java` | Los seis métodos con sus anotaciones, y el **constructor**: inyección por constructor, campo `final` |
| `PacienteService.java` | El método `crear`, donde vive la regla del DNI repetido |
| `PacienteRequest.java` | Las validaciones declaradas: `@NotBlank`, `@Pattern`, `@Past`, `@Email` |

**La frase clave:** «el controlador no sabe que un DNI no puede repetirse; eso lo sabe el servicio».

### 3. Su prueba

```powershell
.\mvnw.cmd test "-Dtest=PacienteControllerTest"
```

Pruebas para citar:

> `GET /api/pacientes?texto= filtra tambien por parte del DNI`
> `GET /api/pacientes/{id} devuelve la ficha completa`

### 4. Su Postman — carpeta 2, en este orden exacto

| # | Solicitud | Resultado | Qué decir |
|---|---|---|---|
| 1 | Listar pacientes | `200` | — |
| 2 | Buscar por texto y estado | `200` | «Esto es `@RequestParam`» |
| 3 | Consultar un paciente | `200` | «Esto es `@PathVariable`» |
| 4 | Consultar uno inexistente | `404` | «El recurso no existe y la API lo dice con el código correcto» |
| 5 | Crear paciente | `201` | **Headers → `Location`** |
| 6 | Crear con DNI inválido | `400` | «Falló el formato, ni entró al controlador» |
| 7 | Crear con DNI repetido | `400` | «El formato estaba bien, pero el servicio detectó que ya existe» |
| 8 | Actualizar (PUT) y antecedentes (PATCH) | `200` | «Esa es la diferencia entre PUT y PATCH» |
| 9 | Eliminar | `204` | «Sin cuerpo, porque no hay nada que devolver» |

Las solicitudes 6 y 7 son **el momento más valioso de toda la sustentación**: mismo código de estado, dos capas de validación distintas.

### 5. Qué decir

> «Yo trabajé el módulo de pacientes, que es el CRUD completo. La clase se marca con `@RestController` y `@RequestMapping("/api/pacientes")`, y cada método se asocia a un verbo con su anotación.
>
> Fíjense en el constructor: el controlador no crea el servicio, lo recibe. Eso es inyección de dependencias por constructor, y el campo queda `final`. Lo hacemos así porque deja explícito de qué depende la clase y facilita las pruebas.
>
> Y miren estos dos errores seguidos: los dos son 400, pero el primero lo rechaza la validación del DTO antes de entrar al método, y el segundo lo rechaza el servicio porque el DNI ya existe. Son dos niveles distintos.»

### Preguntas probables

- **¿`@Controller` o `@RestController`?** `@RestController` es `@Controller` + `@ResponseBody`.
- **¿Por qué constructor y no `@Autowired`?** Campo `final`, dependencias explícitas, objeto siempre completo, pruebas más fáciles.
- **¿Quién crea el servicio?** El contenedor de Spring, porque está anotado con `@Service`.

---

# Turno 3 — Citas, agenda y dashboard

**Tema:** Semana 4, verbos HTTP, parámetros y `ResponseEntity`.

### 1. Su commit

`b47f9a3 feat: modulo de citas, agenda y dashboard`

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `CitaController.java` | 12 endpoints: los cinco verbos y varios `PATCH` sobre subrecursos |
| `CitaService.java` | La comprobación de solapamiento de horarios |
| `DashboardResponse.java` | Un DTO que compone información de varios módulos |

### 3. Su prueba

```powershell
.\mvnw.cmd test "-Dtest=CitaControllerTest"
```

Pruebas para citar:

> `POST /api/citas devuelve 400 si el horario del odontologo se cruza`
> `PATCH /api/citas/{id}/estado devuelve 400 si la cita ya esta CANCELADA`
> `POST /api/citas/{id}/reprogramar crea una cita nueva ligada a la original`

### 4. Su Postman — carpeta 3

| Solicitud | Resultado | Qué decir |
|---|---|---|
| Agenda del día | `200` | — |
| Crear cita | `201` | — |
| **Crear cita con horario cruzado** | `400` | «Un odontólogo no puede tener dos citas solapadas» |
| Crear cita con fecha pasada | `400` | «Validación `@Future` en el DTO» |
| Confirmar y reprogramar | `200` / `201` | «Reprogramar crea una cita nueva ligada a la original; la anterior no se borra» |
| Indicadores del dashboard | `200` | «Un solo GET que compone datos de todos los módulos» |

### 5. Qué decir

> «Mi módulo es el de citas y el dashboard. Aquí se usan los cinco verbos: GET para consultar, POST para crear, PUT para reemplazar, PATCH para cambios puntuales como confirmar o cancelar, y DELETE para eliminar.
>
> La regla central es que un odontólogo no puede tener dos citas solapadas. Si intento crear una cita a la misma hora, el servicio la rechaza con un 400 y un mensaje que explica el motivo.
>
> Y el dashboard es un solo GET que arma un DTO con los indicadores de todos los módulos: pacientes registrados, citas del día, tratamientos activos, pagos pendientes e ingresos del mes.»

### Preguntas probables

- **¿PUT o PATCH?** `PUT` reemplaza el recurso completo; `PATCH` modifica solo una parte.
- **¿Por qué `201` y no `200` al crear?** Porque se creó un recurso nuevo, y va con la cabecera `Location`.
- **¿Qué pasa si cancelo una cita ya cancelada?** Devuelve `400`: desde `CANCELADA` o `ATENDIDA` no se vuelve a otro estado.

---

# Turno 4 — Historia clínica y odontograma

**Tema:** Semana 4, validación de datos y manejo de errores.

### 1. Su commit

`d5ff041 feat: modulo de historia clinica y odontograma`

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `HistoriaClinicaController.java` | **No tiene `PUT` ni `DELETE`**, y es a propósito |
| `OdontogramaRegistroRequest.java` | `@Min(11)` y `@Max(85)`: la notación FDI declarada en el DTO |
| `ApiExceptionHandler.java` | `@RestControllerAdvice`: ningún controlador tiene `try/catch` |

### 3. Su prueba

```powershell
.\mvnw.cmd test "-Dtest=HistoriaClinicaControllerTest"
```

Pruebas para citar:

> `Una consulta anulada sigue apareciendo en el historial del paciente`
> `POST odontograma devuelve 400 con la pieza 99, que no existe en la notacion FDI`
> `Un segundo estado de la misma pieza deja el ultimo como estado actual`

### 4. Su Postman — carpeta 4

| Solicitud | Resultado | Qué decir |
|---|---|---|
| Registrar una consulta | `201` | — |
| Registrar sin diagnóstico | `400` | «El diagnóstico es obligatorio: `@NotBlank` en el DTO» |
| Anular una consulta con su motivo | `200` | «No hay DELETE: se anula, no se borra» |
| **Ver el historial otra vez** | `200` | «La consulta anulada sigue ahí» ← el momento clave |
| Registrar la pieza 99 | `400` | «Esa pieza no existe en la notación FDI» |
| Historial de una pieza | `200` | «Cada cambio crea un registro; el actual es el último» |

### 5. Qué decir

> «Mi módulo es la historia clínica y el odontograma, y tomamos una decisión de diseño que quiero explicar: **este módulo no tiene PUT ni DELETE a propósito**. La información clínica no se sobrescribe ni se borra. Si una consulta se registró por error, se anula indicando el motivo, y sigue apareciendo en el historial.
>
> Lo mismo en el odontograma: cada cambio de estado de una pieza crea un registro nuevo, así se puede reconstruir toda la historia de esa pieza.
>
> Sobre la validación: las piezas dentales siguen la notación FDI, del 11 al 48 y del 51 al 85. Si mando la 99, el DTO la rechaza con un 400. Y ese error, como todos los de la API, sale de una sola clase: `ApiExceptionHandler`. Ningún controlador tiene `try/catch`.»

### Preguntas probables

- **¿Qué hace `@Valid`?** Dispara la validación del DTO antes de que el método se ejecute.
- **¿Por qué un manejador global?** Para no repetir `try/catch` y que toda la API devuelva la misma estructura de error.
- **¿Por qué no se puede editar una historia clínica?** Porque es un registro clínico: se anula con su motivo, no se sobrescribe.

---

# Turno 5 — Tratamientos y pagos

**Tema:** Semana 3, desarrollo guiado por pruebas.

Es el turno que más valora el curso, porque el TDD fue el tema de esa semana.

### 1. Su commit

`7aab87d feat: modulo de tratamientos y pagos`

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `PagoService.java`, línea 90 | El `if` de la regla del saldo, y la constante `TOLERANCIA` |
| `PagoControllerTest.java` | La prueba de esa misma regla, escrita **antes** que el código |

### 3. Su prueba — aquí está el peso de su turno

```powershell
.\mvnw.cmd test "-Dtest=PagoControllerTest"
```

**La demostración en vivo, tres pasos:**

1. Ejecutar: **13 pruebas en verde**.
2. Comentar el bloque `if (monto - saldo > TOLERANCIA)` en `PagoService` (`Ctrl+K`, `Ctrl+C`), guardar y volver a ejecutar:
   ```
   PagoControllerTest.pagoQueExcedeElSaldoDevuelve400 <<< FAILURE!
   java.lang.AssertionError: Status expected:<400> but was:<201>
   ```
3. Descomentar (`Ctrl+K`, `Ctrl+U`), guardar, ejecutar: **verde otra vez**.

Si algo sale mal: `git checkout -- ProyectoFinal/backend/src/main/java/com/utp/odontologia/service/PagoService.java`

### 4. Su Postman — carpeta 5

| Solicitud | Resultado | Qué decir |
|---|---|---|
| Crear tratamiento | `201` | — |
| Registrar un pago parcial | `201` | — |
| Estado de cuenta | `200` | «El saldo se calcula, no se guarda» |
| **Pago mayor al saldo** | `400` | «Esta es la regla que acabamos de ver como prueba» |
| Eliminar tratamiento con pagos | `400` | «No se borra un tratamiento que ya tiene pagos» |

### 5. Qué decir

> «Mi módulo es tratamientos y pagos, y lo trabajé con TDD, que fue el tema de la semana 3.
>
> El ciclo es Red, Green, Refactor: primero escribo una prueba que falla, después el código mínimo que la hace pasar, y al final mejoro el diseño con la prueba de red de seguridad.
>
> La regla es que un pago nunca puede superar el saldo pendiente. Esta prueba la escribí **antes** que el código. Miren lo que pasa si quito la validación… [ejecuta] …la prueba falla y dice exactamente qué se rompió: esperaba un 400 y recibió un 201. La restauro… y vuelve a verde.
>
> Usamos JUnit 5 para ejecutar, Mockito para los dobles y MockMvc para lanzar peticiones HTTP simuladas contra los controladores reales, sin abrir un puerto.»

### Preguntas probables

- **¿Qué es MockMvc?** Simula peticiones HTTP con todo el contexto de Spring activo, sin abrir un puerto.
- **¿Mockito o MockMvc?** Mockito aísla una clase de sus colaboradores; MockMvc prueba el endpoint completo.
- **¿Unitarias o de integración?** De integración. Las unitarias puras están planteadas como siguiente paso.
- **¿El TDD no es más lento?** Al inicio sí; a cambio, cada regla queda verificada y se puede refactorizar sin miedo.

---

# Turno 6 — Archivos clínicos y reportes

**Tema:** Semana 4, pruebas de la API con Postman.

Cierra la sustentación.

### 1. Su commit

`cc80d20 feat: modulo de archivos clinicos y reportes`

### 2. Su código

| Archivo | Qué señalar |
|---|---|
| `ArchivoController.java` | El `POST` con `consumes = MULTIPART_FORM_DATA_VALUE`: la única solicitud que no es JSON |
| `ArchivoResponse.java` | **No expone la ruta física** del archivo |
| `ReporteService.java` | Los seis reportes por período |

### 3. Su prueba

```powershell
.\mvnw.cmd test "-Dtest=ArchivoControllerTest"
```

Pruebas para citar:

> `La respuesta nunca expone la ubicacion fisica del archivo`
> `POST /api/archivos devuelve 400 si la extension no esta permitida`

**Y el cierre de todo el proyecto:**

```powershell
.\mvnw.cmd test
```

> `Tests run: 156, Failures: 0, Errors: 0` · `BUILD SUCCESS`

### 4. Su Postman — carpeta 6

| Solicitud | Resultado | Qué decir |
|---|---|---|
| Subir un archivo clínico | `201` | «La única que no viaja en JSON: usa `multipart/form-data`» |
| Descargar el archivo | `200` | «El binario solo se obtiene por este endpoint» |
| Reporte de ingresos | `200` | «Agrupado por método de pago y por mes» |
| Reporte con fechas invertidas | `400` | «La fecha inicial no puede ser posterior a la final» |
| **Run collection** | 65 en verde | El cierre |

### 5. Qué decir

> «Yo hice archivos clínicos y reportes. La subida de archivos es la única solicitud que no viaja en JSON: usa `multipart/form-data`. Solo aceptamos ciertas extensiones y un máximo de diez megabytes, y la ruta física del archivo nunca aparece en las respuestas: solo se accede al contenido por el endpoint de descarga.
>
> Para cerrar, ejecuto toda la colección de Postman: son 65 solicitudes que cubren los seis módulos, cada una con su prueba automática… [Run collection] …75 verificaciones, ninguna falla.
>
> Y la suite del backend: [`mvnw test`] 156 pruebas, cero fallos.
>
> En estas cuatro semanas construimos la API completa del sistema: 69 endpoints en 11 controladores, con arquitectura por capas, validación, manejo centralizado de errores y 156 pruebas automatizadas. La base de datos con JPA, la seguridad con JWT, Angular y el despliegue entran en las siguientes unidades, y el proyecto ya está estructurado para recibirlas. Gracias.»

### Preguntas probables

- **¿Cómo se prueba una subida de archivo?** Con `multipart/form-data`, enviando el archivo y sus metadatos como partes de la solicitud.
- **¿Dónde se guardan los archivos?** En una carpeta local `uploads/`. Migrar a la nube está previsto para la etapa de despliegue.
- **¿Qué pasa si se reinicia el servidor?** Los datos en memoria se pierden y se recargan los de demostración: la persistencia en base de datos es de la unidad siguiente.

---

# Reglas del equipo durante la exposición

- Cada uno expone **de pie**, cede el turno nombrando al siguiente y al tema que sigue.
- Nadie interrumpe el turno de otro. Las precisiones, al final.
- Las preguntas sobre un módulo **las responde su responsable**, aunque otro sepa la respuesta.
- Si preguntan por JPA, JWT, Angular o despliegue: «corresponde a las semanas 6 a 10 (o 11 a 15, o 16 a 18) y lo dejamos preparado así…».
- **Ensayar completo una vez con cronómetro.** El error más común es que los dos primeros turnos se coman el tiempo de los últimos.

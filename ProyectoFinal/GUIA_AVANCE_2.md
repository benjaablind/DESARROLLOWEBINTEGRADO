# Guía del Avance 2 — Semanas 6 a 10

Base de datos, JPQL, transacciones, seguridad con roles y JWT, sobre el mismo sistema del Avance 1.

La idea que tiene que quedar en la sustentación: **la API del Avance 1 no se reescribió**. Se cambió lo que hay debajo —dónde se guardan los datos y quién puede usarlos— y los 69 endpoints conservan sus rutas. Solo cambió el contrato de la autenticación: el login ahora devuelve un token, y el perfil y la subida de archivos toman al usuario del token. Las 156 pruebas del Avance 1 siguen en verde, ahora sobre una base de datos.

| Semana | Tema | Dónde se ve en el código |
|--------|------|--------------------------|
| 6 | JPA, Hibernate, CRUD con MySQL | `model/` (entidades), `repository/RepositorioBase.java` |
| 7 | JPQL y transacciones | `@Query` en `repository/`, `@Transactional` en `service/` |
| 8 | Spring Security, roles y permisos | `config/SecurityConfig.java`, `security/CustomUserDetailsService.java` |
| 9 | JWT | `config/JwtConfig.java`, `security/TokenService.java`, `controller/AuthController.java` |
| 10 | Avance 2: integración | Esta guía, la colección de Postman, las 178 pruebas y el frontend con JWT |

---

## Antes de sustentar

1. MySQL encendido y la base creada con `backend/sql/01_crear_base_datos.sql`.
2. Backend levantado con `backend\iniciar.ps1`. Pide la contraseña de MySQL y genera la clave del JWT.
3. Postman con la colección importada. Enviar **1. Login correcto** una vez: guarda el token que usan las demás.
4. MySQL Workbench abierto con `backend/sql/02_verificar_base_datos.sql`.
5. Una terminal en `backend/` para correr `.\mvnw.cmd test` al final.

---

## Orden de la demostración

1. **La base de datos existe de verdad.** En Workbench, `SHOW TABLES` (14 tablas) y la consulta de claves foráneas (21). Ninguna tabla se creó a mano: las generó Hibernate desde las entidades.
2. **Sin token no se entra.** Postman → *Sin token (401)*.
3. **Con token, según el rol.** *Login correcto* → cualquier consulta da 200. *Asistente intenta registrar un paciente* → 403.
4. **Cada integrante, su módulo** (sección siguiente).
5. **La persistencia.** Detener el backend, volver a iniciarlo y repetir una consulta: los datos siguen ahí. En el Avance 1 se perdían.
6. **Cierre.** *Run collection*: 73 solicitudes, 84 verificaciones, 0 fallas. Luego `.\mvnw.cmd test`: 178 pruebas, 0 fallos.

---

## Qué explica cada integrante

Cada uno sigue los mismos pasos del Avance 1: **su código → su prueba → su Postman**, enfocado en lo nuevo de su módulo.

### Integrante 1 — Usuarios, roles y autenticación (semanas 8 y 9)

**Código:**
- `SecurityConfig`: la cadena de filtros. Las reglas por rol van de la más específica a la más general, terminando en `anyRequest().authenticated()`.
- `CustomUserDetailsService`: convierte el `Usuario` de la base en algo que Spring Security entiende. El rol `ODONTOLOGO` se vuelve la autoridad `ROLE_ODONTOLOGO`.
- `UsuarioService`: las contraseñas se guardan con `passwordEncoder.encode()` y se verifican con `matches()`. Ya no se comparan en texto plano.
- `TokenService` + `JwtConfig`: el login firma un JWT (HS256) con `sub`, `iss`, `iat`, `exp` y `roles`. La clave sale de la variable de entorno `JWT_SECRET_BASE64`, nunca del repositorio.
- `UsuarioController`: `@PreAuthorize("hasRole('ADMINISTRADOR') or @usuarioActual.es(#id)")`. Cada uno cambia solo su propia contraseña.

**Prueba:** `SeguridadTest`, 17 casos: token alterado, token vencido, la matriz de cada rol, y que la contraseña se guarde como `$2a$...`.

**Postman (carpeta 1):** *Login correcto* (mostrar el token), *Login con credenciales incorrectas (401)*, *Sin token (401)*, *Asistente intenta registrar un paciente (403)*, *Perfil del usuario del token*.

**Para lucirse:** el estado "usuario inactivo" se revisa **después** de validar la contraseña (`setPostAuthenticationChecks`). Así nadie puede averiguar qué cuentas existen probando nombres.

### Integrante 2 — Pacientes (semanas 6 y 7)

**Código:**
- `Paciente` es una `@Entity` con `dni` único.
- `Antecedentes` es una entidad propia unida con `@OneToOne(cascade = ALL, orphanRemoval = true)`, y sus cuatro listas son `@ElementCollection`, cada una en su tabla.
- `PacienteRepository`: `findByDni` es una **consulta derivada** (Spring Data la arma desde el nombre); `buscarCoincidencias` es **JPQL** con `LIKE` sobre nombre completo y DNI.

**Prueba:** `TransaccionesYPersistenciaTest.eliminarPacienteConCitasRespondeConflicto`. Borrar un paciente con citas responde **409**: la clave foránea lo impide. En el Avance 1 se borraba y dejaba citas huérfanas.

**Postman (carpeta 2):** buscar por texto, y mostrar en Workbench la fila del paciente nuevo.

### Integrante 3 — Citas, agenda y dashboard (semanas 6 y 7)

**Código:**
- `Cita` guarda `pacienteId` y `odontologoId` como columnas, y al lado tiene un `@ManyToOne` de solo lectura (`insertable = false, updatable = false`) sobre la misma columna. Eso genera la clave foránea sin cambiar cómo el servicio asigna las referencias.
- `CitaRepository.listarEntre`: JPQL con `BETWEEN`.
- `CitaService.reprogramar`: dos escrituras (crea la nueva, marca la original) en una sola transacción.
- `DashboardService` es `@Transactional(readOnly = true)` completo, y los ingresos del mes los suma la base (`SUM`).

**Prueba:** `TransaccionesYPersistenciaTest.reprogramarHaceRollbackCompleto`. Se hace fallar el segundo paso a propósito y se comprueba que la cita nueva **tampoco** quedó. Sin `@Transactional` la prueba falla (se verificó): quedan dos citas activas.

**Postman (carpeta 3):** reprogramar y ver la cita nueva con `citaOriginalId`.

### Integrante 4 — Historia clínica y odontograma (semanas 6 y 7)

**Código:**
- `HistoriaClinica` y `RegistroPieza` son entidades con sus claves foráneas a paciente y odontólogo.
- `OdontogramaRepository.listarHistorialDePieza`: JPQL con `ORDER BY r.fecha DESC, r.id DESC`. El primer resultado es el estado actual de la pieza.
- La historia sigue siendo inmutable: sin `PUT` ni `DELETE`, solo anular.

**Seguridad:** la información clínica solo la ven administrador y odontólogo. Un asistente recibe 403.

**Postman (carpeta 4):** registrar un estado en el odontograma y ver el historial de la pieza.

### Integrante 5 — Tratamientos y pagos (semana 7)

**Código:**
- **`BigDecimal`**: `precio` y `monto` pasaron de `double` a `BigDecimal`, columna `DECIMAL(12,2)`. Antes `PagoService` comparaba con una tolerancia de 0.001 para esquivar errores de coma flotante; ya no hace falta. `@Digits` rechaza más de dos decimales.
- `SesionTratamiento` es un `@Embeddable` en una `@ElementCollection`: su id es local al tratamiento (1, 2, 3...), no una clave primaria.
- `PagoRepository.totalPagadoDeTratamiento`: JPQL `COALESCE(SUM(p.monto), 0)`.
- **Bloqueo pesimista** en `PagoService.registrar`: `tratamientoRepository.buscarParaActualizar()` hace `SELECT ... FOR UPDATE`. Si dos cobros llegan a la vez, el segundo espera y ve el saldo ya reducido, así nunca se cobra de más. Es la "I" (aislamiento) de ACID.

**Postman (carpeta 5):** pago que excede el saldo → 400 con el saldo exacto.

### Integrante 6 — Archivos clínicos y reportes (semanas 7 a 9)

**Código:**
- En la base solo van los **metadatos** del archivo (`Archivo` es entidad); el binario sigue en disco, con nombre UUID.
- **Los archivos ya no son públicos** (lo pedía el plan de trabajo): solo administrador y odontólogo pueden listarlos o descargarlos.
- `ArchivoController.subir` toma al usuario que sube **del token** (`UsuarioActual`). Ya no recibe un `usuarioId` que el cliente pudiera falsificar.
- `PagoRepository.totalesPorMetodoEntre`: JPQL con **`GROUP BY` y expresión constructora** (`SELECT new ...TotalPorMetodo(p.metodo, SUM(p.monto))`). La base calcula el reporte de ingresos por método.
- `ReporteService` es `readOnly` completo. El historial consolidado exige acceso clínico, porque incluye consultas.

**Postman (carpeta 6):** *Reporte de ingresos*, *Recepcionista no accede a archivos clínicos (403)*, *Recepcionista no ve el historial clínico consolidado (403)*, *Archivos sin token (401)*.

---

## Preguntas probables

| Pregunta | Respuesta corta |
|----------|-----------------|
| ¿Diferencia entre JPA, Hibernate y Spring Data JPA? | JPA es la especificación (anotaciones). Hibernate la implementa y genera el SQL. Spring Data JPA crea los repositorios a partir de interfaces. |
| ¿Por qué los servicios no cambiaron? | `RepositorioBase` extiende `JpaRepository` y conserva los nombres que ya usaban (`guardar` llama a `save`, `listar` a `findAll`). Es la ventaja de la arquitectura por capas. |
| ¿Por qué no `@ManyToOne` directo en vez del id? | Se agregó al lado, de solo lectura, sobre la misma columna: da la clave foránea y permite navegar en JPQL sin cambiar los servicios ni los DTOs. |
| ¿Qué es JPQL? | SQL sobre entidades y atributos (`Paciente p`, `p.dni`), no sobre tablas. Hibernate lo traduce al SQL de MySQL. |
| ¿Cuándo hace rollback `@Transactional`? | Con `RuntimeException` (o `Error`) que se propague fuera del método. Las excepciones *checked* no, salvo `rollbackFor`. |
| ¿Para qué `readOnly = true`? | Declara que el método solo lee: Hibernate omite la detección de cambios y queda clara la intención. |
| ¿Por qué `BigDecimal`? | `double` es binario: 0.1 + 0.2 no da 0.3. Con dinero se necesita aritmética exacta. |
| ¿Qué riesgo tiene `ddl-auto=update`? | Cambia el esquema sin control de versiones ni marcha atrás. En producción se usa `validate` con Flyway o Liquibase. |
| ¿Por qué BCrypt? | Es un hash con *salt* y lento a propósito: no se puede revertir y encarece la fuerza bruta. |
| ¿401 o 403? | 401: no sé quién eres (sin token o token inválido). 403: sé quién eres, pero tu rol no puede. |
| ¿El JWT está cifrado? | No, está **firmado**: cualquiera lee su contenido, pero nadie puede alterarlo sin la clave. Por eso no lleva datos sensibles. |
| ¿Por qué la clave del JWT va en una variable de entorno? | Con HS256 la misma clave firma y verifica: quien la tenga puede fabricar un token de administrador. |
| ¿Cómo se cierra sesión con JWT? | El cliente descarta el token. El servidor no guarda tokens, así que sigue valiendo hasta que vence (60 minutos). |
| ¿Por qué CSRF desactivado? | El token viaja en el encabezado `Authorization`, que el navegador no adjunta solo. CSRF explota cookies automáticas. |
| ¿Y el frontend? | Ya usa el JWT: un interceptor de Angular agrega `Authorization: Bearer` a cada petición, y si el backend responde 401 vuelve al login. El menú y los botones siguen la misma matriz de permisos del servidor. |

---

## Lo que queda pendiente, dicho con honestidad

- **Revocar tokens:** desactivar a un usuario no invalida el token que ya tiene; vale hasta que vence.
- **El token en el navegador:** se guarda en `localStorage`. Es lo habitual en una SPA, pero un ataque XSS podría leerlo; la alternativa es una cookie `HttpOnly`, que obliga a volver a activar la protección CSRF.

Resuelto después de la primera versión del avance: el frontend ya envía el token y su menú coincide con la matriz del servidor, y la recepción tiene `GET /api/usuarios/odontologos` (solo id y nombre) para elegir al odontólogo al agendar.

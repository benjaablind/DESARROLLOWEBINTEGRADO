# Guía del Avance 3 — Semanas 11 a 15

Frontend con Angular: componentes, rutas, formularios y consumo de la API protegida con JWT, sobre el mismo backend del Avance 2.

La idea que tiene que quedar en la sustentación: **el frontend valida lo mismo que el backend, y cada regla tiene una prueba**. Cada formulario repite en el navegador las reglas que el servidor ya aplica (`@NotBlank`, `@Pattern`, `@Future`, el saldo de un pago, la notación FDI del odontograma...). El usuario ve el error antes de enviar, y el backend sigue teniendo la última palabra. El backend no cambió en este avance; el frontend pasó de 2 a **270 pruebas**.

| Semana | Tema | Dónde se ve en el código |
|--------|------|--------------------------|
| 11 | Componentes, SCSS y data binding | `frontend/src/app/paginas/`, `layout/`, `styles.scss` con tokens de diseño |
| 12 | Rutas y comunicación entre componentes | `app.routes.ts` (carga diferida, `canActivate`), `pacientes/:id` |
| 13 | Formularios | `core/validators/` (un archivo por módulo), `FormArray` en pacientes y archivos |
| 14 | Servicios y HttpClient | `core/services/` sobre `api.service.ts`, `auth.interceptor.ts`, `finalize()`, los `*.service.spec.ts` |
| 15 | Avance 3: integración | Esta guía y la aplicación completa contra la API con JWT |

---

## Antes de sustentar

1. MySQL encendido (XAMPP) y la base creada con `backend/sql/01_crear_base_datos.sql`.
2. Backend levantado con `backend\iniciar.ps1`.
3. Frontend: en `frontend/`, `npm install` (solo la primera vez) y `npm start`. Abrir `http://localhost:4200` y entrar con `admin` / `admin123`.
4. Una terminal en `frontend/` para correr las pruebas de cada módulo.
5. En GitHub, la pestaña de commits de la rama del avance abierta.

Comando para correr solo las pruebas de un módulo, desde `ProyectoFinal\frontend`:

```powershell
npx ng test --watch=false --include="src/app/**/paciente*.spec.ts"
```

---

## Orden de la demostración

1. **La aplicación consume la API real.** Login con `admin`. Abrir DevTools → Network: cada petición lleva `Authorization: Bearer ...` (lo agrega `auth.interceptor.ts`).
2. **Sin sesión no se entra.** Cerrar sesión e intentar abrir `/pacientes`: el guard devuelve al login.
3. **Cada integrante, su módulo** (sección siguiente): su commit → su código → su prueba → su demo en pantalla.
4. **Cierre.** `npx ng test --watch=false`: 28 archivos, 270 pruebas, 0 fallos. `npx ng build`: compila sin advertencias.

---

## Qué explica cada integrante

Los seis aplicaron lo mismo a su módulo:

- **Semana 13:** validadores propios (`ValidatorFn`) en `core/validators/`. Devuelven `null` para el valor vacío, así la obligatoriedad la resuelve `Validators.required`. Los errores solo se muestran con `touched` o al intentar enviar.
- **Semana 14:** `finalize()` apaga los indicadores de carga tanto si la petición responde como si falla. Además, pruebas de cada servicio con `HttpTestingController`, que verifican método, URL, parámetros y cuerpo de cada petición.

| Turno | Integrante | Módulo | Commit | Pruebas del módulo |
|---|---|---|---|---|
| 1 | yeffdx | Usuarios y autenticación | `b1b93f6` | 41 |
| 2 | Angeloxdo | Pacientes | `8beaadb` | 46 |
| 3 | benjaablind | Citas, agenda y dashboard | `261112e` | 56 |
| 4 | JeaneliR | Historia clínica y odontograma | `7c503b3` | 37 |
| 5 | Dadalijo | Tratamientos y pagos | `400966a` | 45 |
| 6 | Marco | Archivos clínicos y reportes | `77affca` | 43 |

### Integrante 1 — Usuarios y autenticación

**Código:**
- `core/validators/usuarios.validators.ts`:
  - `noEnBlanco` replica `@NotBlank`: `Validators.required` acepta `"   "` y el backend no.
  - `longitudRecortada` mide `@Size(4,30)` sobre el valor recortado, que es el que se envía.
  - Dos validadores de **grupo**: `passwordsIguales` (confirmación) y `passwordDistinta` (la nueva no puede ser igual a la actual, como en `UsuarioService`).
- `paginas/usuarios/usuarios.ts`: la confirmación de contraseña nunca se envía al backend.

**Prueba:** `--include="src/app/**/usuario*.spec.ts" --include="src/app/**/auth*.spec.ts"`. Para leer en voz alta: *«cambiar contraseña bloquea "nueva igual a la actual" y luego envía solo passwordActual y passwordNuevo»*.

**Demo:** crear un usuario con confirmación distinta → «Las contraseñas no coinciden», sin petición en Network.

### Integrante 2 — Pacientes

**Código:**
- `core/validators/pacientes.validators.ts`: `dniValido` (`@Pattern \d{8}`) y `fechaPasada` (`@Past`). Recibe el reloj como parámetro para probarlo con una fecha fija.
- Los antecedentes (alergias, enfermedades, medicamentos y cirugías) son un **`FormArray`** porque `AntecedentesRequest` los recibe como `List<String>`. Antes eran texto separado por comas, y «Hipertensión, controlada» se partía en dos.

**Prueba:** `--include="src/app/**/paciente*.spec.ts"`.

**Demo:** en la ficha, editar antecedentes → *Agregar* dos alergias, *Quitar* una y guardar.

### Integrante 3 — Citas, agenda y dashboard

**Código:**
- `core/validators/citas.validators.ts`: `fechaHoraFutura` es un validador de **grupo**, porque la fecha y la hora van en campos separados y solo juntas forman el instante que exige `@Future`. `fechaHoraDistinta` impide reprogramar a la misma hora.
- `core/services/cita.service.ts`: **corrección de un error**. `CitaController` lee `asistio` y `motivo` con `@RequestParam`, pero el frontend los mandaba en el cuerpo. Marcar asistencia respondía 400 y el motivo de cancelación se perdía.

**Prueba:** `--include="src/app/**/cita*.spec.ts" --include="src/app/**/dashboard*.spec.ts"`.

**Demo:** marcar *Asistió* en una cita → 200 en Network, con `?asistio=true` en la URL.

### Integrante 4 — Historia clínica y odontograma

**Código:**
- `core/validators/odontograma.validators.ts`: `piezaFdi` es la misma regla que `OdontogramaService.validarNumeroPieza`. Las piezas permanentes son 11–18 a 41–48 y las deciduas, 51–55 a 81–85.
- `core/validators/historia.validators.ts`: `citaDelPaciente` es un validador de **grupo** que replica `HistoriaClinicaService.validarCita`: la cita elegida debe ser del paciente de la consulta.
- Longitudes máximas tomadas de las columnas de la base. Sin ellas, un texto largo terminaba en un 500.

**Prueba:** `--include="src/app/**/histori*.spec.ts" --include="src/app/**/odontograma*.spec.ts"`.

**Demo:** registrar una consulta sin diagnóstico → mensaje específico, sin petición.

### Integrante 5 — Tratamientos y pagos

**Código:**
- `core/validators/tratamientos.validators.ts`: `importe` replica `@Positive` y `@Digits(10,2)`. Antes `10.555` pasaba en el frontend.
- `core/validators/pagos.validators.ts`: `montoDentroDelSaldo` es un validador de **grupo** que compara en céntimos, para evitar errores de punto flotante (`0.1 + 0.2`). Reemplaza la «confirmación en dos clics», que dejaba enviar un pago que `PagoService` siempre rechaza.
- La fecha de la sesión es obligatoria (`@NotNull` en `SesionRequest`); antes se enviaba vacía y daba 400.

**Prueba:** `--include="src/app/**/tratamiento*.spec.ts" --include="src/app/**/pago*.spec.ts"`.

**Demo:** registrar un pago mayor al saldo → error bajo el monto y ninguna petición.

### Integrante 6 — Archivos clínicos y reportes

**Código:**
- `core/validators/archivos.validators.ts`: `archivoPermitido` repite el orden de `ArchivoService.subir`: archivo vacío, lista blanca de extensiones y tope de 10 MB.
- `paginas/archivos/archivos.ts`, subida múltiple con **`FormArray`**:
  - Cada archivo elegido es una fila con su tipo y descripción.
  - Se suben uno por uno con `concatMap`. Si uno falla, los demás siguen (`catchError` por fila) y solo queda la fila fallida, con el mensaje del backend.
- Miniaturas y *Descargar* piden el binario con `HttpClient`. La descarga exige JWT y un `<img src>` no envía el token, por eso desde el Avance 2 respondían 401.
- `core/validators/reportes.validators.ts`: `rangoFechas` es un validador de **grupo** que replica `ReporteService.validarRango`. Con el rango invertido, el reporte no se pide.

**Prueba:** `--include="src/app/**/archivo*.spec.ts" --include="src/app/**/reporte*.spec.ts"`. Para leer en voz alta: *«sube uno por uno; si uno falla, deja solo esa fila con el mensaje del backend»*.

**Demo:**
1. Elegir a la vez un PNG, un `.exe` y un PDF.
2. La fila del `.exe` aparece en rojo y no deja subir.
3. *Quitar* esa fila → *Subir 2 archivos*: dos `POST /archivos` con 201.

---

## Cómo se ve en la rúbrica

| Aspecto | Dónde está |
|---------|-----------|
| API RESTful con Spring Boot | `backend/`, 69 endpoints documentados en `DOCUMENTACION_API.md` |
| Back-end con base de datos | MySQL con JPA e Hibernate (Avance 2) |
| Front-end con Angular | `frontend/`: 12 pantallas con componentes standalone, signals y formularios reactivos |
| Integración back-end y front-end | `api.service.ts` + `auth.interceptor.ts` con JWT; CORS global en `CorsConfig` |
| Funcionalidades implementadas | Los seis módulos, de punta a punta |
| Pruebas y validaciones | 270 pruebas del frontend y 178 del backend; validadores alineados con los DTO |
| Despliegue | Pendiente: semanas 16 a 18 |

---

## Preguntas probables

- **¿Para qué validar en el frontend si el backend ya valida?** Por la experiencia de usuario: el error aparece al instante, junto al campo, y no se hace una petición que va a fallar. La seguridad sigue en el backend, porque la validación del navegador se puede saltar desde DevTools.
- **¿Por qué un validador devuelve `null` con el campo vacío?** Para que cada validador tenga una sola responsabilidad. Si devolviera error, un campo vacío mostraría dos mensajes: «obligatorio» y «formato inválido».
- **¿Cuándo va un validador en el grupo y no en el control?** Cuando necesita comparar dos campos: contraseña y confirmación, fecha y hora, monto y saldo, desde y hasta.
- **¿Qué hace `finalize()`?** Se ejecuta cuando el `Observable` termina, bien o con error. Así el «Cargando...» se apaga en un solo lugar y nunca se queda colgado.
- **¿Qué prueba `HttpTestingController`?** Que el servicio arma la petición correcta (método, URL, parámetros y cuerpo) sin levantar el backend. `http.verify()` falla si quedó alguna petición sin atender.
- **¿Por qué `concatMap` en la subida múltiple?** Envía un archivo a la vez y en orden. `mergeMap` los mandaría todos juntos y saturaría la conexión con archivos de hasta 10 MB.

---

## Lo que queda pendiente, dicho con honestidad

- **Despliegue (semanas 16 a 18).** La aplicación corre en local.
- **Teléfono del paciente:** el frontend exige de 6 a 15 dígitos, pero el backend solo pide que no esté vacío. Hay que decidir cuál de las dos reglas se mantiene.
- **Tildes:** algunos textos antiguos de la interfaz no las llevan (por ejemplo, «Odontologo»). Los mensajes nuevos sí.

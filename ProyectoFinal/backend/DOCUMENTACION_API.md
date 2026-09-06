# Documentación de la API — Sistema de Gestión Odontológica

**URL base:** `http://localhost:8080/api`
**Formato:** JSON, salvo la subida de archivos que usa `multipart/form-data`.
**Encabezado:** `Content-Type: application/json` en POST, PUT y PATCH.

Todos los errores devuelven la misma estructura:

```json
{
  "estado": 404,
  "mensaje": "No existe paciente con id: 99",
  "ruta": "/api/pacientes/99",
  "fechaHora": "2026-09-04T16:44:26.47"
}
```

Códigos usados: 200 en consultas y actualizaciones, 201 en creación con encabezado `Location`, 204 en borrado, 400 en datos inválidos o reglas de negocio incumplidas, 404 cuando el recurso no existe.

## Autenticación — Integrante 1

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Valida credenciales y devuelve el usuario |
| POST | `/api/auth/logout` | Cierra sesión |
| GET | `/api/auth/perfil?usuarioId=` | Datos del usuario indicado |

Sin JWT todavía, el servidor no guarda sesión. El frontend conserva al usuario y envía su identificador cuando hace falta. La protección real llega con Spring Security.

```json
POST /api/auth/login
{ "usuario": "admin", "password": "admin123" }
```

## Usuarios y roles — Integrante 1

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/usuarios?rol=&activo=` | Lista con filtros opcionales |
| GET | `/api/usuarios/{id}` | Consulta uno |
| POST | `/api/usuarios` | Crea. Rechaza usuario o correo repetido |
| PUT | `/api/usuarios/{id}` | Reemplaza los datos |
| PATCH | `/api/usuarios/{id}/password` | Cambia la contraseña |
| PATCH | `/api/usuarios/{id}/activar` | Reactiva la cuenta |
| PATCH | `/api/usuarios/{id}/desactivar` | Baja lógica |
| DELETE | `/api/usuarios/{id}` | Elimina |
| GET | `/api/usuarios/{id}/permisos` | Permisos según el rol |

Roles: `ADMINISTRADOR`, `ODONTOLOGO`, `RECEPCIONISTA`, `ASISTENTE`. La contraseña nunca aparece en las respuestas.

## Pacientes — Integrante 2

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pacientes?texto=&estado=` | Lista, busca por nombre o documento |
| GET | `/api/pacientes/{id}` | Ficha completa con antecedentes |
| GET | `/api/pacientes/dni/{dni}` | Busca por documento |
| POST | `/api/pacientes` | Crea. Rechaza documento repetido |
| PUT | `/api/pacientes/{id}` | Reemplaza los datos |
| PATCH | `/api/pacientes/{id}/antecedentes` | Actualiza solo los antecedentes |
| PATCH | `/api/pacientes/{id}/estado` | Activa o desactiva |
| DELETE | `/api/pacientes/{id}` | Elimina |

El documento debe tener ocho dígitos. La edad se calcula sola a partir de la fecha de nacimiento.

## Citas y agenda — Integrante 3

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/citas?dia=&pacienteId=&odontologoId=&estado=` | Lista con filtros combinables |
| GET | `/api/citas/agenda?dia=` | Agenda de un día |
| GET | `/api/citas/disponibilidad?odontologoId=&dia=` | Horas ya ocupadas |
| GET | `/api/citas/{id}` | Consulta una |
| POST | `/api/citas` | Crea. Rechaza cruce de horario |
| PUT | `/api/citas/{id}` | Reemplaza los datos |
| PATCH | `/api/citas/{id}/estado` | Cambia el estado |
| PATCH | `/api/citas/{id}/confirmar` | Confirma |
| PATCH | `/api/citas/{id}/asistencia?asistio=` | Registra si asistió |
| PATCH | `/api/citas/{id}/cancelar?motivo=` | Cancela |
| POST | `/api/citas/{id}/reprogramar` | Crea una cita nueva y marca la anterior |
| DELETE | `/api/citas/{id}` | Elimina |

Estados: `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `CANCELADA`, `NO_ASISTIO`, `REPROGRAMADA`.

Reglas: un odontólogo no puede tener dos citas que se solapen. Las canceladas y reprogramadas no ocupan la agenda. Desde `CANCELADA` o `ATENDIDA` no se puede volver a otro estado. Al reprogramar se crea una cita nueva que apunta a la original.

## Dashboard — Integrante 3

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/dashboard` | Indicadores generales |

Devuelve pacientes registrados, citas del día, citas pendientes, tratamientos activos, pagos pendientes, ingresos del mes, próximas citas, pacientes recientes, tratamientos activos con su saldo, alertas y el resumen de actividad del mes.

## Historia clínica — Integrante 4

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/historias/paciente/{pacienteId}` | Historial completo |
| GET | `/api/historias/{id}` | Consulta una |
| POST | `/api/historias` | Registra una consulta |
| PATCH | `/api/historias/{id}/anular` | Anula indicando el motivo |

No hay PUT ni DELETE a propósito. La información histórica no se sobrescribe ni se borra. Una consulta registrada por error se anula, pero sigue apareciendo en el historial.

## Odontograma — Integrante 4

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/odontograma/paciente/{pacienteId}` | Estado actual de todas las piezas |
| GET | `/api/odontograma/paciente/{pacienteId}/pieza/{numeroPieza}` | Historial de una pieza |
| GET | `/api/odontograma/paciente/{pacienteId}/resumen` | Cuántas piezas hay en cada estado |
| POST | `/api/odontograma` | Registra un estado nuevo |

Estados: `SANO`, `CARIES`, `OBTURADO`, `ENDODONCIA`, `CORONA`, `IMPLANTE`, `PROTESIS`, `FRACTURADO`, `SELLANTE`, `EXTRACCION_INDICADA`, `AUSENTE`.

Cada cambio crea un registro nuevo, nunca edita el anterior. El estado actual de una pieza es su último registro. Los números siguen la notación FDI: del 11 al 48 en dentición permanente y del 51 al 85 en dentición decidua. Cualquier otro número devuelve 400.

## Tratamientos — Integrante 5

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/tratamientos?pacienteId=&estado=&soloActivos=` | Lista con filtros |
| GET | `/api/tratamientos/{id}` | Detalle con sesiones y saldo |
| POST | `/api/tratamientos` | Crea |
| PUT | `/api/tratamientos/{id}` | Reemplaza los datos |
| PATCH | `/api/tratamientos/{id}/estado` | Cambia el estado |
| POST | `/api/tratamientos/{id}/sesiones` | Agrega una sesión |
| PATCH | `/api/tratamientos/{id}/sesiones/{sesionId}` | Marca la sesión como realizada |
| DELETE | `/api/tratamientos/{id}/sesiones/{sesionId}` | Elimina una sesión |
| DELETE | `/api/tratamientos/{id}` | Elimina, solo si no tiene pagos |

Estados: `PENDIENTE`, `APROBADO`, `EN_PROCESO`, `COMPLETADO`, `CANCELADO`.

Reglas: un tratamiento completado o cancelado no se modifica ni acepta sesiones nuevas. Al completarlo se registra la fecha de fin. No se elimina un tratamiento que ya tiene pagos.

## Pagos — Integrante 5

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pagos?pacienteId=&tratamientoId=` | Lista con filtros |
| GET | `/api/pagos/{id}` | Consulta uno |
| GET | `/api/pagos/tratamiento/{tratamientoId}` | Historial de un tratamiento |
| GET | `/api/pagos/estado-cuenta/{pacienteId}` | Total, pagado y saldo del paciente |
| POST | `/api/pagos` | Registra un pago total o parcial |
| DELETE | `/api/pagos/{id}` | Elimina |

Métodos: `EFECTIVO`, `TARJETA`, `TRANSFERENCIA`, `YAPE`, `PLIN`.

Reglas: el monto no puede superar el saldo pendiente. Un tratamiento cancelado no admite pagos. El saldo nunca queda negativo.

## Archivos clínicos — Integrante 6

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/archivos?pacienteId=&tipo=` | Lista con filtros |
| GET | `/api/archivos/{id}` | Metadatos de uno |
| GET | `/api/archivos/paciente/{pacienteId}` | Archivos de un paciente |
| POST | `/api/archivos` | Sube un archivo, `multipart/form-data` |
| GET | `/api/archivos/{id}/descargar` | Descarga el binario |
| PUT | `/api/archivos/{id}` | Actualiza solo los metadatos |
| DELETE | `/api/archivos/{id}` | Elimina el registro y el binario |

Tipos: `RADIOGRAFIA`, `FOTOGRAFIA`, `INFORME`, `CONSENTIMIENTO`, `RECETA`, `DOCUMENTO`.

Campos de la subida: `archivo`, `pacienteId`, `usuarioId`, `tipo`, `descripcion`, `historiaClinicaId`.

Reglas: extensiones permitidas jpg, jpeg, png, gif, bmp, webp, pdf, doc, docx y txt. Tamaño máximo diez megabytes. En disco cada archivo recibe un nombre único, así dos archivos con el mismo nombre no se pisan. La ruta física nunca aparece en las respuestas y solo se accede al contenido por el endpoint de descarga.

## Reportes — Integrante 6

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/reportes/pacientes?desde=&hasta=` | Pacientes registrados |
| GET | `/api/reportes/citas?desde=&hasta=` | Citas del período agrupadas por estado |
| GET | `/api/reportes/tratamientos?desde=&hasta=` | Realizados y pendientes con sus montos |
| GET | `/api/reportes/ingresos?desde=&hasta=` | Ingresos por método de pago y por mes |
| GET | `/api/reportes/pagos-pendientes` | Tratamientos con saldo |
| GET | `/api/reportes/historial/{pacienteId}` | Consolidado de un paciente |

Las fechas van en formato `yyyy-MM-dd` y son opcionales. Si la fecha inicial es posterior a la final, la respuesta es 400.

## Ejemplos con curl

En Windows conviene usar `curl.exe` para evitar el alias de PowerShell.

```bash
# Iniciar sesión
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"usuario\":\"admin\",\"password\":\"admin123\"}"

# Buscar pacientes por nombre
curl "http://localhost:8080/api/pacientes?texto=torres"

# Agenda de hoy
curl "http://localhost:8080/api/citas/agenda?dia=2026-09-04"

# Registrar un estado en el odontograma
curl -X POST http://localhost:8080/api/odontograma -H "Content-Type: application/json" -d "{\"pacienteId\":2,\"odontologoId\":2,\"numeroPieza\":46,\"estado\":\"OBTURADO\",\"superficie\":\"Oclusal\"}"

# Registrar un pago
curl -X POST http://localhost:8080/api/pagos -H "Content-Type: application/json" -d "{\"tratamientoId\":1,\"monto\":150.0,\"metodo\":\"EFECTIVO\"}"

# Subir un archivo clínico
curl -X POST http://localhost:8080/api/archivos -F "archivo=@radiografia.png" -F "pacienteId=2" -F "usuarioId=2" -F "tipo=RADIOGRAFIA"

# Reporte de ingresos del año
curl "http://localhost:8080/api/reportes/ingresos?desde=2026-01-01&hasta=2026-12-31"
```

# Cómo se ve la ejecución de la colección

Salida real de ejecutar las 65 solicitudes contra el backend en local. En Postman se obtiene lo mismo con clic derecho sobre la colección → **Run collection** → **Run**, solo que en una tabla visual: cada solicitud en su fila, con su código de estado y el resultado de su prueba.

Lo que hay que mirar al final: **65 solicitudes, 75 aserciones, 0 fallidas**.

Para ejecutarla desde la terminal, como aquí, hace falta Newman:

```bash
npm install -g newman
cd ProyectoFinal/postman
newman run Sistema_Odontologico.postman_collection.json --working-dir .
```

`--working-dir .` es lo que permite que la solicitud de subida encuentre `radiografia-ejemplo.png`.

---

```
(node:4624) [DEP0176] DeprecationWarning: fs.F_OK is deprecated, use fs.constants.F_OK instead
(Use `node --trace-deprecation ...` to show where the warning was created)
newman

Sistema de Gestion Odontologica - API REST

□ 1. Autenticacion y usuarios - Integrante 1
└ Login correcto
  POST http://localhost:8080/api/auth/login [200 OK, 549B, 34ms]
  √  Responde 200
  √  El usuario queda autenticado

└ Login con credenciales incorrectas (400)
  POST http://localhost:8080/api/auth/login [400 Bad Request, 359B, 6ms]
  √  Responde 400

└ Listar usuarios
  GET http://localhost:8080/api/usuarios [200 OK, 1.34kB, 5ms]
  √  Responde 200

└ Listar solo odontologos
  GET http://localhost:8080/api/usuarios?rol=ODONTOLOGO [200 OK, 684B, 3ms]
  √  Responde 200

└ Crear usuario (201)
  POST http://localhost:8080/api/usuarios [201 Created, 526B, 5ms]
  √  Responde 201
  √  Guarda el id en la variable usuarioCreadoId

└ Crear usuario repetido (400)
  POST http://localhost:8080/api/usuarios [400 Bad Request, 389B, 3ms]
  √  Responde 400

└ Crear usuario con email invalido (400)
  POST http://localhost:8080/api/usuarios [400 Bad Request, 367B, 5ms]
  √  Responde 400

└ Permisos segun el rol
  GET http://localhost:8080/api/usuarios/1/permisos [200 OK, 619B, 5ms]
  √  Responde 200

└ Desactivar usuario (baja logica)
  PATCH http://localhost:8080/api/usuarios/10/desactivar [200 OK, 473B, 4ms]
  √  Responde 200

└ Eliminar usuario (204)
  DELETE http://localhost:8080/api/usuarios/10 [204 No Content, 201B, 3ms]
  √  Responde 204

□ 2. Pacientes - Integrante 2
└ Listar pacientes
  GET http://localhost:8080/api/pacientes [200 OK, 973B, 3ms]
  √  Responde 200

└ Buscar por texto y estado
  GET http://localhost:8080/api/pacientes?texto=torres&estado=ACTIVO [200 OK, 374B, 2ms]
  √  Responde 200

└ Consultar un paciente
  GET http://localhost:8080/api/pacientes/1 [200 OK, 799B, 3ms]
  √  Responde 200

└ Consultar un paciente inexistente (404)
  GET http://localhost:8080/api/pacientes/999 [404 Not Found, 387B, 3ms]
  √  Responde 404

└ Crear paciente (201 + Location)
  POST http://localhost:8080/api/pacientes [201 Created, 791B, 2ms]
  √  Responde 201
  √  Guarda el id en la variable pacienteCreadoId
  √  Devuelve la cabecera Location

└ Crear con DNI invalido (400 por validacion)
  POST http://localhost:8080/api/pacientes [400 Bad Request, 358B, 3ms]
  √  Responde 400

└ Crear con DNI repetido (400 por regla de negocio)
  POST http://localhost:8080/api/pacientes [400 Bad Request, 378B, 5ms]
  √  Responde 400

└ Actualizar paciente (PUT)
  PUT http://localhost:8080/api/pacientes/11 [200 OK, 744B, 4ms]
  √  Responde 200

└ Actualizar solo los antecedentes (PATCH)
  PATCH http://localhost:8080/api/pacientes/11/antecedentes [200 OK, 888B, 3ms]
  √  Responde 200

└ Desactivar paciente (PATCH estado)
  PATCH http://localhost:8080/api/pacientes/11/estado [200 OK, 890B, 3ms]
  √  Responde 200

└ Eliminar paciente (204)
  DELETE http://localhost:8080/api/pacientes/11 [204 No Content, 201B, 2ms]
  √  Responde 204

□ 3. Citas, agenda y dashboard - Integrante 3
└ Listar citas
  GET http://localhost:8080/api/citas [200 OK, 4.46kB, 3ms]
  √  Responde 200

└ Agenda del dia
  GET http://localhost:8080/api/citas/agenda?dia=2026-09-09 [200 OK, 1.33kB, 2ms]
  √  Responde 200

└ Horas ocupadas de un odontologo
  GET http://localhost:8080/api/citas/disponibilidad?odontologoId=2&dia=2026-09-09 [200 OK, 270B, 2ms]
  √  Responde 200

└ Crear cita (201)
  POST http://localhost:8080/api/citas [201 Created, 640B, 3ms]
  √  Responde 201
  √  Guarda el id en la variable citaCreadaId

└ Crear cita con horario cruzado (400)
  POST http://localhost:8080/api/citas [400 Bad Request, 368B, 3ms]
  √  Responde 400

└ Crear cita con fecha pasada (400)
  POST http://localhost:8080/api/citas [400 Bad Request, 368B, 4ms]
  √  Responde 400

└ Confirmar cita
  PATCH http://localhost:8080/api/citas/16/confirmar [200 OK, 611B, 5ms]
  √  Responde 200

└ Reprogramar cita
  POST http://localhost:8080/api/citas/16/reprogramar [201 Created, 666B, 3ms]
  √  Responde 201
  √  Guarda el id en la variable citaReprogramadaId

└ Cancelar la cita reprogramada
  PATCH http://localhost:8080/api/citas/17/cancelar?motivo=El paciente aviso que no podra asistir [200 OK, 644B, 3ms]
  √  Responde 200

└ Eliminar cita
  DELETE http://localhost:8080/api/citas/17 [204 No Content, 201B, 2ms]
  √  Responde 204

└ Indicadores del dashboard
  GET http://localhost:8080/api/dashboard [200 OK, 4.12kB, 3ms]
  √  Responde 200

□ 4. Historia clinica y odontograma - Integrante 4
└ Historial completo de un paciente
  GET http://localhost:8080/api/historias/paciente/1 [200 OK, 3.78kB, 3ms]
  √  Responde 200

└ Registrar una consulta (201)
  POST http://localhost:8080/api/historias [201 Created, 967B, 2ms]
  √  Responde 201
  √  Guarda el id en la variable historiaCreadaId

└ Registrar sin diagnostico (400)
  POST http://localhost:8080/api/historias [400 Bad Request, 368B, 2ms]
  √  Responde 400

└ Anular una consulta con su motivo
  PATCH http://localhost:8080/api/historias/8/anular [200 OK, 977B, 4ms]
  √  Responde 200

└ Verificar que la consulta anulada sigue en el historial
  GET http://localhost:8080/api/historias/paciente/1 [200 OK, 4.51kB, 2ms]
  √  Responde 200

└ Odontograma completo del paciente
  GET http://localhost:8080/api/odontograma/paciente/2 [200 OK, 776B, 2ms]
  √  Responde 200

└ Registrar estado de una pieza (201)
  POST http://localhost:8080/api/odontograma [201 Created, 528B, 2ms]
  √  Responde 201

└ Registrar una pieza inexistente (400)
  POST http://localhost:8080/api/odontograma [400 Bad Request, 384B, 2ms]
  √  Responde 400

└ Historial de una pieza dental
  GET http://localhost:8080/api/odontograma/paciente/2/pieza/46 [200 OK, 1.87kB, 5ms]
  √  Responde 200

└ Resumen de piezas por estado
  GET http://localhost:8080/api/odontograma/paciente/2/resumen [200 OK, 267B, 2ms]
  √  Responde 200

□ 5. Tratamientos y pagos - Integrante 5
└ Listar tratamientos activos
  GET http://localhost:8080/api/tratamientos?soloActivos=true [200 OK, 2.66kB, 3ms]
  √  Responde 200

└ Detalle con sesiones y saldo
  GET http://localhost:8080/api/tratamientos/1 [200 OK, 1.05kB, 2ms]
  √  Responde 200

└ Crear tratamiento (201)
  POST http://localhost:8080/api/tratamientos [201 Created, 750B, 3ms]
  √  Responde 201
  √  Guarda el id en la variable tratamientoCreadoId

└ Crear con precio negativo (400)
  POST http://localhost:8080/api/tratamientos [400 Bad Request, 368B, 4ms]
  √  Responde 400

└ Agregar una sesion
  POST http://localhost:8080/api/tratamientos/11/sesiones [201 Created, 822B, 3ms]
  √  Responde 201

└ Cambiar el estado a EN_PROCESO
  PATCH http://localhost:8080/api/tratamientos/11/estado [200 OK, 826B, 2ms]
  √  Responde 200

└ Registrar un pago parcial (201)
  POST http://localhost:8080/api/pagos [201 Created, 553B, 3ms]
  √  Responde 201
  √  Guarda el id en la variable pagoCreadoId

└ Registrar un pago mayor al saldo (400)
  POST http://localhost:8080/api/pagos [400 Bad Request, 395B, 3ms]
  √  Responde 400

└ Estado de cuenta del paciente
  GET http://localhost:8080/api/pagos/estado-cuenta/1 [200 OK, 1.77kB, 5ms]
  √  Responde 200

└ Eliminar un tratamiento con pagos (400)
  DELETE http://localhost:8080/api/tratamientos/11 [400 Bad Request, 389B, 2ms]
  √  Responde 400

└ Eliminar el pago
  DELETE http://localhost:8080/api/pagos/12 [204 No Content, 201B, 4ms]
  √  Responde 204

└ Eliminar el tratamiento (204)
  DELETE http://localhost:8080/api/tratamientos/11 [204 No Content, 201B, 2ms]
  √  Responde 204

□ 6. Archivos clinicos y reportes - Integrante 6
└ Subir un archivo clinico (201)
  POST http://localhost:8080/api/archivos (node:4624) [DEP0044] DeprecationWarning: The `util.isArray` API is deprecated. Please use `Array.isArray()` instead.
[201 Created, 790B, 18ms]
  √  Responde 201
  √  Guarda el id en la variable archivoCreadoId

└ Listar archivos de un paciente
  GET http://localhost:8080/api/archivos?pacienteId=1 [200 OK, 739B, 4ms]
  √  Responde 200

└ Metadatos de un archivo
  GET http://localhost:8080/api/archivos/4 [200 OK, 737B, 2ms]
  √  Responde 200

└ Descargar el archivo
  GET http://localhost:8080/api/archivos/4/descargar [200 OK, 121.08kB, 3ms]
  √  Responde 200

└ Eliminar el archivo (204)
  DELETE http://localhost:8080/api/archivos/4 [204 No Content, 201B, 2ms]
  √  Responde 204

└ Reporte de pacientes registrados
  GET http://localhost:8080/api/reportes/pacientes?desde=2026-01-01&hasta=2026-12-31 [200 OK, 1.34kB, 3ms]
  √  Responde 200

└ Reporte de citas por periodo
  GET http://localhost:8080/api/reportes/citas?desde=2026-01-01&hasta=2026-12-31 [200 OK, 2.58kB, 3ms]
  √  Responde 200

└ Reporte de ingresos
  GET http://localhost:8080/api/reportes/ingresos?desde=2026-01-01&hasta=2026-12-31 [200 OK, 512B, 3ms]
  √  Responde 200

└ Reporte de pagos pendientes
  GET http://localhost:8080/api/reportes/pagos-pendientes [200 OK, 1.15kB, 2ms]
  √  Responde 200

└ Historial consolidado de un paciente
  GET http://localhost:8080/api/reportes/historial/1 [200 OK, 3.11kB, 3ms]
  √  Responde 200

└ Reporte con fechas invertidas (400)
  GET http://localhost:8080/api/reportes/citas?desde=2026-12-31&hasta=2026-01-01 [400 Bad Request, 387B, 3ms]
  √  Responde 400

┌─────────────────────────┬─────────────────┬─────────────────┐
│                         │        executed │          failed │
├─────────────────────────┼─────────────────┼─────────────────┤
│              iterations │               1 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│                requests │              65 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│            test-scripts │              65 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│      prerequest-scripts │              69 │               0 │
├─────────────────────────┼─────────────────┼─────────────────┤
│              assertions │              75 │               0 │
├─────────────────────────┴─────────────────┴─────────────────┤
│ total run duration: 5.8s                                    │
├─────────────────────────────────────────────────────────────┤
│ total data received: 166.64kB (approx)                      │
├─────────────────────────────────────────────────────────────┤
│ average response time: 3ms [min: 2ms, max: 34ms, s.d.: 4ms] │
└─────────────────────────────────────────────────────────────┘
```

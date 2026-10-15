# Cómo se ve la ejecución de la colección

Salida real de ejecutar las 73 solicitudes contra el backend en local, ya con la seguridad JWT de las semanas 8 y 9. En Postman se obtiene lo mismo con clic derecho sobre la colección → **Run collection** → **Run**, solo que en una tabla visual: cada solicitud en su fila, con su código de estado y el resultado de su prueba.

Lo que hay que mirar al final: **73 solicitudes, 84 aserciones, 0 fallidas**.

La primera solicitud (**Login correcto**) obtiene el JWT y lo guarda en la variable `{{token}}`; todas las demás lo envían como `Authorization: Bearer`. Por eso la colección se ejecuta completa y en orden. Las solicitudes nuevas de seguridad verifican los 401 (sin token) y los 403 (rol sin permiso).

Para ejecutarla desde la terminal, como aquí, hace falta Newman:

```bash
npm install -g newman
cd ProyectoFinal/postman
newman run Sistema_Odontologico.postman_collection.json --working-dir .
```

`--working-dir .` es lo que permite que la solicitud de subida encuentre `radiografia-ejemplo.png`.

---

```
newman

Sistema de Gestion Odontologica - API REST

□ 1. Autenticacion y usuarios - Integrante 1
└ Login correcto
  POST http://localhost:8080/api/auth/login [200 OK, 930B, 591ms]
  √  Responde 200
  √  Devuelve un JWT y nunca la contrasena

└ Login con credenciales incorrectas (401)
  POST http://localhost:8080/api/auth/login [401 Unauthorized, 559B, 105ms]
  √  Responde 401: credenciales invalidas

└ Sin token (401)
  GET http://localhost:8080/api/pacientes [401 Unauthorized, 699B, 21ms]
  √  Responde 401: falta el token

└ Perfil del usuario del token
  GET http://localhost:8080/api/auth/perfil [200 OK, 652B, 21ms]
  √  Responde 200: perfil del token
  √  Es el usuario que inicio sesion

└ Login como asistente
  POST http://localhost:8080/api/auth/login [200 OK, 908B, 90ms]
  √  Responde 200 con un JWT

└ Asistente intenta registrar un paciente (403)
  POST http://localhost:8080/api/pacientes [403 Forbidden, 582B, 6ms]
  √  Responde 403: el rol no tiene permiso

└ Listar usuarios
  GET http://localhost:8080/api/usuarios [200 OK, 1.51kB, 38ms]
  √  Responde 200

└ Listar solo odontologos
  GET http://localhost:8080/api/usuarios?rol=ODONTOLOGO [200 OK, 852B, 11ms]
  √  Responde 200

└ Crear usuario (201)
  POST http://localhost:8080/api/usuarios [201 Created, 694B, 128ms]
  √  Responde 201
  √  Guarda el id en la variable usuarioCreadoId

└ Crear usuario repetido (400)
  POST http://localhost:8080/api/usuarios [400 Bad Request, 560B, 12ms]
  √  Responde 400

└ Crear usuario con email invalido (400)
  POST http://localhost:8080/api/usuarios [400 Bad Request, 537B, 13ms]
  √  Responde 400

└ Permisos segun el rol
  GET http://localhost:8080/api/usuarios/1/permisos [200 OK, 789B, 70ms]
  √  Responde 200

└ Desactivar usuario (baja logica)
  PATCH http://localhost:8080/api/usuarios/6/desactivar [200 OK, 641B, 10ms]
  √  Responde 200

└ Eliminar usuario (204)
  DELETE http://localhost:8080/api/usuarios/6 [204 No Content, 371B, 24ms]
  √  Responde 204

□ 2. Pacientes - Integrante 2
└ Listar pacientes
  GET http://localhost:8080/api/pacientes [200 OK, 1.14kB, 22ms]
  √  Responde 200

└ Buscar por texto y estado
  GET http://localhost:8080/api/pacientes?texto=torres&estado=ACTIVO [200 OK, 544B, 20ms]
  √  Responde 200

└ Consultar un paciente
  GET http://localhost:8080/api/pacientes/1 [200 OK, 911B, 23ms]
  √  Responde 200

└ Consultar un paciente inexistente (404)
  GET http://localhost:8080/api/pacientes/999 [404 Not Found, 557B, 7ms]
  √  Responde 404

└ Crear paciente (201 + Location)
  POST http://localhost:8080/api/pacientes [201 Created, 959B, 42ms]
  √  Responde 201
  √  Guarda el id en la variable pacienteCreadoId
  √  Devuelve la cabecera Location

└ Crear con DNI invalido (400 por validacion)
  POST http://localhost:8080/api/pacientes [400 Bad Request, 528B, 6ms]
  √  Responde 400

└ Crear con DNI repetido (400 por regla de negocio)
  POST http://localhost:8080/api/pacientes [400 Bad Request, 548B, 14ms]
  √  Responde 400

└ Actualizar paciente (PUT)
  PUT http://localhost:8080/api/pacientes/7 [200 OK, 912B, 16ms]
  √  Responde 200

└ Actualizar solo los antecedentes (PATCH)
  PATCH http://localhost:8080/api/pacientes/7/antecedentes [200 OK, 1.06kB, 26ms]
  √  Responde 200

└ Desactivar paciente (PATCH estado)
  PATCH http://localhost:8080/api/pacientes/7/estado [200 OK, 1.06kB, 17ms]
  √  Responde 200

└ Eliminar paciente (204)
  DELETE http://localhost:8080/api/pacientes/7 [204 No Content, 371B, 13ms]
  √  Responde 204

□ 3. Citas, agenda y dashboard - Integrante 3
└ Listar citas
  GET http://localhost:8080/api/citas [200 OK, 2.92kB, 32ms]
  √  Responde 200

└ Agenda del dia
  GET http://localhost:8080/api/citas/agenda?dia=2026-10-15 [200 OK, 1.5kB, 23ms]
  √  Responde 200

└ Horas ocupadas de un odontologo
  GET http://localhost:8080/api/citas/disponibilidad?odontologoId=2&dia=2026-10-15 [200 OK, 440B, 8ms]
  √  Responde 200

└ Crear cita (201)
  POST http://localhost:8080/api/citas [201 Created, 808B, 42ms]
  √  Responde 201
  √  Guarda el id en la variable citaCreadaId

└ Crear cita con horario cruzado (400)
  POST http://localhost:8080/api/citas [400 Bad Request, 538B, 17ms]
  √  Responde 400

└ Crear cita con fecha pasada (400)
  POST http://localhost:8080/api/citas [400 Bad Request, 538B, 9ms]
  √  Responde 400

└ Confirmar cita
  PATCH http://localhost:8080/api/citas/8/confirmar [200 OK, 779B, 17ms]
  √  Responde 200

└ Reprogramar cita
  POST http://localhost:8080/api/citas/8/reprogramar [201 Created, 833B, 34ms]
  √  Responde 201
  √  Guarda el id en la variable citaReprogramadaId

└ Cancelar la cita reprogramada
  PATCH http://localhost:8080/api/citas/9/cancelar?motivo=El paciente aviso que no podra asistir [200 OK, 811B, 11ms]
  √  Responde 200

└ Eliminar cita
  DELETE http://localhost:8080/api/citas/9 [204 No Content, 371B, 12ms]
  √  Responde 204

└ Indicadores del dashboard
  GET http://localhost:8080/api/dashboard [200 OK, 3.66kB, 102ms]
  √  Responde 200

□ 4. Historia clinica y odontograma - Integrante 4
└ Historial completo de un paciente
  GET http://localhost:8080/api/historias/paciente/1 [200 OK, 1.05kB, 20ms]
  √  Responde 200

└ Registrar una consulta (201)
  POST http://localhost:8080/api/historias [201 Created, 1.14kB, 20ms]
  √  Responde 201
  √  Guarda el id en la variable historiaCreadaId

└ Registrar sin diagnostico (400)
  POST http://localhost:8080/api/historias [400 Bad Request, 538B, 5ms]
  √  Responde 400

└ Anular una consulta con su motivo
  PATCH http://localhost:8080/api/historias/4/anular [200 OK, 1.15kB, 15ms]
  √  Responde 200

└ Verificar que la consulta anulada sigue en el historial
  GET http://localhost:8080/api/historias/paciente/1 [200 OK, 1.78kB, 11ms]
  √  Responde 200

└ Odontograma completo del paciente
  GET http://localhost:8080/api/odontograma/paciente/2 [200 OK, 953B, 13ms]
  √  Responde 200

└ Registrar estado de una pieza (201)
  POST http://localhost:8080/api/odontograma [201 Created, 698B, 19ms]
  √  Responde 201

└ Registrar una pieza inexistente (400)
  POST http://localhost:8080/api/odontograma [400 Bad Request, 554B, 4ms]
  √  Responde 400

└ Historial de una pieza dental
  GET http://localhost:8080/api/odontograma/paciente/2/pieza/46 [200 OK, 1.15kB, 12ms]
  √  Responde 200

└ Resumen de piezas por estado
  GET http://localhost:8080/api/odontograma/paciente/2/resumen [200 OK, 437B, 13ms]
  √  Responde 200

□ 5. Tratamientos y pagos - Integrante 5
└ Listar tratamientos activos
  GET http://localhost:8080/api/tratamientos?soloActivos=true [200 OK, 2.84kB, 27ms]
  √  Responde 200

└ Detalle con sesiones y saldo
  GET http://localhost:8080/api/tratamientos/1 [200 OK, 1.22kB, 12ms]
  √  Responde 200

└ Crear tratamiento (201)
  POST http://localhost:8080/api/tratamientos [201 Created, 921B, 26ms]
  √  Responde 201
  √  Guarda el id en la variable tratamientoCreadoId

└ Crear con precio negativo (400)
  POST http://localhost:8080/api/tratamientos [400 Bad Request, 538B, 4ms]
  √  Responde 400

└ Agregar una sesion
  POST http://localhost:8080/api/tratamientos/7/sesiones [201 Created, 993B, 21ms]
  √  Responde 201

└ Cambiar el estado a EN_PROCESO
  PATCH http://localhost:8080/api/tratamientos/7/estado [200 OK, 997B, 16ms]
  √  Responde 200

└ Registrar un pago parcial (201)
  POST http://localhost:8080/api/pagos [201 Created, 721B, 30ms]
  √  Responde 201
  √  Guarda el id en la variable pagoCreadoId

└ Registrar un pago mayor al saldo (400)
  POST http://localhost:8080/api/pagos [400 Bad Request, 565B, 8ms]
  √  Responde 400

└ Estado de cuenta del paciente
  GET http://localhost:8080/api/pagos/estado-cuenta/1 [200 OK, 1.94kB, 24ms]
  √  Responde 200

└ Eliminar un tratamiento con pagos (400)
  DELETE http://localhost:8080/api/tratamientos/7 [400 Bad Request, 558B, 14ms]
  √  Responde 400

└ Eliminar el pago
  DELETE http://localhost:8080/api/pagos/8 [204 No Content, 371B, 15ms]
  √  Responde 204

└ Eliminar el tratamiento (204)
  DELETE http://localhost:8080/api/tratamientos/7 [204 No Content, 371B, 13ms]
  √  Responde 204

□ 6. Archivos clinicos y reportes - Integrante 6
└ Subir un archivo clinico (201)
[201 Created, 968B, 88ms]
  √  Responde 201
  √  Guarda el id en la variable archivoCreadoId

└ Listar archivos de un paciente
  GET http://localhost:8080/api/archivos?pacienteId=1 [200 OK, 916B, 12ms]
  √  Responde 200

└ Metadatos de un archivo
  GET http://localhost:8080/api/archivos/1 [200 OK, 914B, 18ms]
  √  Responde 200

└ Descargar el archivo
  GET http://localhost:8080/api/archivos/1/descargar [200 OK, 121.25kB, 12ms]
  √  Responde 200

└ Eliminar el archivo (204)
  DELETE http://localhost:8080/api/archivos/1 [204 No Content, 371B, 8ms]
  √  Responde 204

└ Reporte de pacientes registrados
  GET http://localhost:8080/api/reportes/pacientes?desde=2026-01-01&hasta=2026-12-31 [200 OK, 1.5kB, 25ms]
  √  Responde 200

└ Reporte de citas por periodo
  GET http://localhost:8080/api/reportes/citas?desde=2026-01-01&hasta=2026-12-31 [200 OK, 2.02kB, 23ms]
  √  Responde 200

└ Reporte de ingresos
  GET http://localhost:8080/api/reportes/ingresos?desde=2026-01-01&hasta=2026-12-31 [200 OK, 691B, 32ms]
  √  Responde 200

└ Reporte de pagos pendientes
  GET http://localhost:8080/api/reportes/pagos-pendientes [200 OK, 1.34kB, 45ms]
  √  Responde 200

└ Historial consolidado de un paciente
  GET http://localhost:8080/api/reportes/historial/1 [200 OK, 1.88kB, 29ms]
  √  Responde 200

└ Reporte con fechas invertidas (400)
  GET http://localhost:8080/api/reportes/citas?desde=2026-12-31&hasta=2026-01-01 [400 Bad Request, 557B, 4ms]
  √  Responde 400

└ Login como recepcionista
  POST http://localhost:8080/api/auth/login [200 OK, 914B, 92ms]
  √  Responde 200 con un JWT

└ Recepcionista no accede a archivos clinicos (403)
  GET http://localhost:8080/api/archivos [403 Forbidden, 581B, 3ms]
  √  Responde 403: archivos no publicos

└ Recepcionista no ve el historial clinico consolidado (403)
  GET http://localhost:8080/api/reportes/historial/1 [403 Forbidden, 593B, 4ms]
  √  Responde 403: historial clinico protegido

└ Archivos sin token (401)
  GET http://localhost:8080/api/archivos [401 Unauthorized, 698B, 3ms]
  √  Responde 401: falta el token

┌─────────────────────────┬───────────────────┬──────────────────┐
│                         │          executed │           failed │
├─────────────────────────┼───────────────────┼──────────────────┤
│              iterations │                 1 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│                requests │                73 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│            test-scripts │                73 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│      prerequest-scripts │                77 │                0 │
├─────────────────────────┼───────────────────┼──────────────────┤
│              assertions │                84 │                0 │
├─────────────────────────┴───────────────────┴──────────────────┤
│ total run duration: 8.7s                                       │
├────────────────────────────────────────────────────────────────┤
│ total data received: 157.62kB (approx)                         │
├────────────────────────────────────────────────────────────────┤
│ average response time: 32ms [min: 3ms, max: 591ms, s.d.: 70ms] │
└────────────────────────────────────────────────────────────────┘
```

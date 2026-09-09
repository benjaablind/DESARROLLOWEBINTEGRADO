### Inicio de sesion valido (Integrante 1)

```http
POST /auth/login
Content-Type: application/json

{"usuario":"admin","password":"admin123"}
```

```json
HTTP 200
{
  "autenticado": true,
  "usuario": {
    "id": 1,
    "nombres": "Marco",
    "apellidos": "Gonzales del Valle",
    "nombreCompleto": "Marco Gonzales del Valle",
    "email": "admin@clinica.pe",
    "usuario": "admin",
    "rol": "ADMINISTRADOR",
    "activo": true,
    "fechaRegistro": "2026-09-07T23:12:38.8025937"
  },
  "mensaje": "Autenticacion correcta"
}
```

### Inicio de sesion con credenciales incorrectas (Integrante 1)

```http
POST /auth/login
Content-Type: application/json

{"usuario":"admin","password":"clave-mala"}
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "Usuario o contrasena incorrectos",
  "ruta": "/api/auth/login",
  "fechaHora": "2026-09-07T23:19:55.1672613"
}
```

### Listado de pacientes con filtro por texto (Integrante 2)

```http
GET /pacientes?texto=torres
```

```json
HTTP 200
[
  {
    "id": 1,
    "dni": "72451890",
    "nombreCompleto": "Ana Maria Torres Quispe",
    "edad": 34,
    "telefono": "987654321",
    "estado": "ACTIVO"
  }
]
```

### Consulta de un paciente inexistente: 404 (Integrante 2)

```http
GET /pacientes/999
```

```json
HTTP 404
{
  "estado": 404,
  "mensaje": "No existe paciente con id: 999",
  "ruta": "/api/pacientes/999",
  "fechaHora": "2026-09-07T23:19:56.2803455"
}
```

### Registro con DNI invalido: 400 por validacion del DTO (Integrante 2)

```http
POST /pacientes
Content-Type: application/json

{"dni":"123","nombres":"Pedro","apellidos":"Gonzales Diaz","fechaNacimiento":"1995-03-15","sexo":"M","telefono":"900111222","email":"pedro@correo.com"}
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "dni: El DNI debe tener 8 digitos",
  "ruta": "/api/pacientes",
  "fechaHora": "2026-09-07T23:19:56.7926592"
}
```

### Registro de paciente valido: 201 Created (Integrante 2)

```http
POST /pacientes
Content-Type: application/json

{"dni":"45781954","nombres":"Pedro Luis","apellidos":"Gonzales Diaz","fechaNacimiento":"1995-03-15","sexo":"M","telefono":"900111222","email":"pedro@correo.com","direccion":"Jr. Union 456","distrito":"Brena","ciudad":"Lima"}
```

```json
HTTP 201
{
  "id": 8,
  "dni": "45781954",
  "nombres": "Pedro Luis",
  "apellidos": "Gonzales Diaz",
  "nombreCompleto": "Pedro Luis Gonzales Diaz",
  "edad": 31,
  "fechaNacimiento": "1995-03-15",
  "sexo": "M",
  "telefono": "900111222",
  "email": "pedro@correo.com",
  "direccion": "Jr. Union 456",
  "distrito": "Brena",
  "ciudad": "Lima",
  "estado": "ACTIVO",
  "antecedentes": {
    "enfermedades": [],
    "alergias": [],
    "medicamentos": [],
    "habitos": [],
    "antecedentesOdontologicos": null,
    "observaciones": null
  },
  "fechaRegistro": "2026-09-07T23:19:57.3596798"
}
```

### Registro con DNI repetido: 400 por regla de negocio (Integrante 2)

```http
POST /pacientes
Content-Type: application/json

{"dni":"45781954","nombres":"Otro","apellidos":"Paciente Prueba","fechaNacimiento":"1990-01-01","sexo":"F","telefono":"900111333","email":"otro@correo.com"}
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "Ya existe un paciente registrado con el DNI 45781954",
  "ruta": "/api/pacientes",
  "fechaHora": "2026-09-07T23:19:57.890009"
}
```

### Agenda del dia (Integrante 3)

```http
GET /citas/agenda?dia=2026-09-08
```

```json
HTTP 200
[]
```

### Indicadores del panel (Integrante 3)

```http
GET /dashboard
```

```json
HTTP 200
{
  "pacientesRegistrados": 8,
  "citasHoy": 3,
  "citasPendientes": 3,
  "tratamientosActivos": 4,
  "pagosPendientes": 5400.0,
  "ingresosDelMes": 1250.0,
  "proximasCitas": [
    {
      "id": 6,
      "pacienteId": 1,
      "pacienteNombre": "Ana Maria Torres Quispe",
      "odontologoId": 2,
      "odontologoNombre": "Lucia Ramos Vega",
      "fechaHora": "2026-09-09T09:30:00",
      "fechaHoraFin": "2026-09-09T10:00:00",
      "duracionMinutos": 30,
      "motivo": "Ajuste de brackets",
      "estado": "PENDIENTE",
      "observaciones": null,
      "citaOriginalId": null,
      "fechaRegistro": "2026-09-07T23:12:38.803597"
    },
    {
      "id": 7,
      "pacienteId": 2,
      "pacienteNombre": "Carlos Alberto Mendoza Rios",
      "odontologoId": 3,
      "odontologoNombre": "Diego Salas Ortiz",
      "fechaHora": "2026-09-12T17:00:00",
      "fechaHoraFin": "2026-09-12T17:45:00",
      "duracionMinutos": 45,
      "motivo": "Endodoncia primera sesion",
      "estado": "CONFIRMADA",
      "observaciones": null,
      "citaOriginalId": null,
      "fechaRegistro": "2026-09-07T23:12:38.803597"
    }
  ],
  "pacientesRecientes": [
    {
      "id": 8,
      "nombreCompleto": 
```

### Odontograma: pieza fuera de la notacion FDI: 400 (Integrante 4)

```http
POST /odontograma
Content-Type: application/json

{"pacienteId":2,"odontologoId":2,"numeroPieza":99,"estado":"OBTURADO","superficie":"Oclusal"}
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "numeroPieza: El numero de pieza debe estar entre 11 y 85",
  "ruta": "/api/odontograma",
  "fechaHora": "2026-09-07T23:19:59.7440924"
}
```

### Odontograma: resumen de piezas por estado (Integrante 4)

```http
GET /odontograma/paciente/2/resumen
```

```json
HTTP 200
{
  "OBTURADO": 1,
  "ENDODONCIA": 1
}
```

### Estado de cuenta de un paciente (Integrante 5)

```http
GET /pagos/estado-cuenta/2
```

```json
HTTP 200
{
  "pacienteId": 2,
  "pacienteNombre": "Carlos Alberto Mendoza Rios",
  "totalTratamientos": 850.0,
  "totalPagado": 550.0,
  "saldoPendiente": 300.0,
  "tratamientos": [
    {
      "id": 1,
      "pacienteId": 2,
      "pacienteNombre": "Carlos Alberto Mendoza Rios",
      "odontologoId": 2,
      "odontologoNombre": "(no disponible)",
      "nombre": "Endodoncia en pieza 46",
      "descripcion": "Tratamiento de conductos en molar inferior derecho",
      "precio": 850.0,
      "estado": "EN_PROCESO",
      "fechaInicio": "2026-08-28",
      "fechaFin": null,
      "observaciones": null,
      "sesiones": [
        {
          "id": 1,
          "numero": 1,
          "fecha": "2026-08-28",
          "descripcion": "Apertura camaral y medicacion",
          "realizada": true,
          "observaciones": null
        },
        {
          "id": 2,
          "numero": 2,
          "fecha": "2026-09-04",
          "descripcion": "Instrumentacion de conductos",
          "realizada": true,
          "observaciones": null
        },
        {
          "id": 3,
          "numero": 3,
          "fecha": "2026-09-12",
          "descripcion": "Obturacion de conductos",
          "rea
```

### Pago mayor al saldo pendiente: 400 (Integrante 5)

```http
POST /pagos
Content-Type: application/json

{"tratamientoId":1,"monto":99999,"metodo":"EFECTIVO"}
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "El monto excede el saldo pendiente del tratamiento: saldo actual S/ 300.00",
  "ruta": "/api/pagos",
  "fechaHora": "2026-09-07T23:20:01.3432945"
}
```

### Reporte de ingresos por periodo (Integrante 6)

```http
GET /reportes/ingresos?desde=2026-01-01&hasta=2026-12-31
```

```json
HTTP 200
{
  "desde": "2026-01-01",
  "hasta": "2026-12-31",
  "totalIngresos": 4070.0,
  "cantidadPagos": 7,
  "porMetodo": {
    "EFECTIVO": 420.0,
    "YAPE": 250.0,
    "TARJETA": 1500.0,
    "TRANSFERENCIA": 1700.0,
    "PLIN": 200.0
  },
  "porMes": {
    "2026-01": 1500.0,
    "2026-07": 200.0,
    "2026-08": 1120.0,
    "2026-09": 1250.0
  }
}
```

### Reporte con rango de fechas invertido: 400 (Integrante 6)

```http
GET /reportes/citas?desde=2026-12-31&hasta=2026-01-01
```

```json
HTTP 400
{
  "estado": 400,
  "mensaje": "La fecha inicial no puede ser posterior a la fecha final",
  "ruta": "/api/reportes/citas",
  "fechaHora": "2026-09-07T23:20:02.4013881"
}
```


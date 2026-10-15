# Colección de Postman — API del Sistema de Gestión Odontológica

`Sistema_Odontologico.postman_collection.json` contiene las 73 solicitudes que prueban la API, organizadas en seis carpetas, una por integrante y por módulo.

## Cómo usarla

1. Levantar el backend desde `ProyectoFinal/backend`. Desde las semanas 6 a 9 necesita MySQL y la clave del JWT; el script hace todo:
   ```powershell
   .\iniciar.ps1
   ```
   Queda en `http://localhost:8080/api` y, la primera vez, carga los datos de demostración en la base. Los detalles están en [../README.md](../README.md).
2. En Postman: **Import** → seleccionar `Sistema_Odontologico.postman_collection.json`.
3. Abrir cualquier solicitud y pulsar **Send**, o ejecutar todo de una vez con clic derecho sobre la colección → **Run collection**.

No hay que configurar nada más. La dirección del servidor está en la variable de colección `baseUrl`, y las fechas y el DNI de prueba se calculan solos antes de cada solicitud.

## Autenticación con JWT (semanas 8 y 9)

La API ya no está abierta: todas las rutas, salvo el login, exigen un token.

- La primera solicitud, **1. Autenticación → Login correcto**, inicia sesión como `admin` y guarda el JWT en la variable de colección `{{token}}`.
- La colección tiene configurada **Authorization → Bearer Token → `{{token}}`**, y todas las solicitudes la heredan.
- Por eso, si se prueba una solicitud suelta, primero hay que enviar **Login correcto** una vez. El token vence a los 60 minutos; basta con volver a enviarla.
- Las solicitudes que prueban otros roles (asistente, recepcionista) hacen su propio login y usan su propia variable de token.

Usuarios de demostración: `admin`/`admin123` (administrador), `lramos`/`lramos123` (odontóloga), `rdiaz`/`rdiaz123` (recepcionista), `jpalma`/`jpalma123` (asistente).

## Cómo está organizada

| Carpeta | Módulo | Solicitudes |
|---|---|---|
| 1 | Autenticación, usuarios y seguridad | 14 |
| 2 | Pacientes | 11 |
| 3 | Citas, agenda y dashboard | 11 |
| 4 | Historia clínica y odontograma | 10 |
| 5 | Tratamientos y pagos | 12 |
| 6 | Archivos clínicos, reportes y su protección | 15 |

Dentro de cada carpeta las solicitudes están en el orden de la sustentación: primero el caso correcto y después el caso de error, para mostrar la validación de datos y el manejador global de excepciones. Cada carpeta crea sus propios datos y elimina lo que creó, así que la colección se puede ejecutar cuantas veces haga falta.

Cada solicitud trae una prueba automática que verifica el código de estado esperado: al enviarla aparece `PASS` en la pestaña **Test Results**.

Las solicitudes nuevas de seguridad están al final de las carpetas 1 y 6: **sin token → 401** y **rol sin permiso → 403** (un asistente que intenta registrar un paciente, una recepcionista que intenta ver archivos clínicos).

## La solicitud de subida de archivo

**«Subir un archivo clínico»**, en la carpeta 6, es la única que no viaja en JSON: usa `multipart/form-data`. Ya viene apuntando a `radiografia-ejemplo.png`, que está en esta misma carpeta, así que normalmente no hay que hacer nada. Ya no envía `usuarioId`: quién sube el archivo se toma del token.

Si Postman avisa que no encuentra el archivo, hay dos salidas: ir a **Body → form-data**, fila `archivo`, pulsar **Select Files** y elegirlo a mano; o dejarlo resuelto de una vez en **Settings → General → Working directory**, apuntando a `ProyectoFinal/postman`.

Las tres solicitudes que dependen de esa subida se omiten solas si el archivo no llegó a subirse, para que una ejecución completa no muestre errores en cadena.

## Cómo se ve al ejecutarla

[EJEMPLO_EJECUCION.md](EJEMPLO_EJECUCION.md) tiene la salida real de una ejecución completa: 73 solicitudes, 84 aserciones, 0 fallidas.

## Alternativa sin Postman

Los mismos endpoints están documentados con ejemplos de `curl` en [../backend/DOCUMENTACION_API.md](../backend/DOCUMENTACION_API.md). En VS Code, la extensión Thunder Client también importa esta colección.

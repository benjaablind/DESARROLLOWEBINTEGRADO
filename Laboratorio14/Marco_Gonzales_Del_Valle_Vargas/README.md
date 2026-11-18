# Laboratorio 14 — Marco Antonio Gonzales del Valle Vargas

**Servicios y consumo de APIs en Angular.** La aplicación `laboratorio-servicios-api` muestra
los usuarios de JSONPlaceholder y permite crearlos, editarlos, cambiarles el correo y
eliminarlos, según la Guía de Laboratorio 14. El componente no conoce la URL ni usa `HttpClient`
directamente: eso es trabajo de `UsuarioService`.

> JSONPlaceholder simula las escrituras: POST, PUT, PATCH y DELETE responden como si hubieran
> funcionado, pero al recargar la lista vuelve a ser la original (sección 3 de la guía).

## Cómo ejecutarlo

```powershell
cd laboratorio-servicios-api
npm install
npm start          # http://localhost:4200/usuarios  (requiere Internet)
npm test           # 21 pruebas unitarias (Vitest, sin Internet)
npx ng build       # build de produccion en dist/
```

> **Requisito de versión:** Angular 22 exige **Node.js 22.22.3+ o 24.15+**. El proyecto se
> generó y se probó con Node 24.22.3, npm 11.19.1, Angular CLI 22.2.7 y Angular 22.2.6.

Comandos de las secciones 8 y 8.2, más las piezas de las actividades complementarias:

```powershell
ng new laboratorio-servicios-api --routing --style=css --ssr=false
ng generate component usuarios --skip-tests
ng generate service usuarios/usuario --skip-tests --type=service
ng generate interface usuarios/usuario model
ng generate environments
ng generate component usuarios/usuario-detalle --skip-tests
ng generate interceptor core/registro --skip-tests --functional
ng generate service core/contador-solicitudes --skip-tests --type=service
```

## Estructura

```
laboratorio-servicios-api/src/
├── environments/environment*.ts         apiUrl de la API (actividad complementaria)
└── app/
    ├── app.config.ts                    provideRouter + provideHttpClient(withInterceptors)
    ├── app.routes.ts                    '', usuarios, usuarios/:id y '**'
    ├── app.ts / app.html / app.css      cabecera con el contador de solicitudes exitosas
    ├── core/
    │   ├── registro-interceptor.ts      registra cada solicitud en consola
    │   └── contador-solicitudes.service.ts
    └── usuarios/
        ├── usuario.model.ts             Usuario y UsuarioEntrada = Omit<Usuario, 'id'>
        ├── usuario.service.ts           GET, GET por id, POST, PUT, PATCH y DELETE
        ├── usuarios.ts / .html / .css   formulario reactivo + tabla + estados
        └── usuario-detalle/             vista /usuarios/:id
```

## Cómo fluye una solicitud

```
Usuarios (componente) --obtenerTodos()--> UsuarioService --http.get--> registroInterceptor --> JSONPlaceholder
JSONPlaceholder --JSON--> interceptor (log + contador) --> Observable --> subscribe() --> signal usuarios --> tabla
```

Cada estado de la interfaz es un `signal`: `cargando`, `guardando`, `mensaje` y `error`. Las
llamadas usan `finalize()`, así que el indicador «Cargando...» o «Procesando...» se apaga tanto si
la operación termina bien como si falla. La aplicación nunca queda cargando para siempre
(sección 20).

## Prueba de las operaciones (sección 18)

Ejecutada con Playwright y Edge contra JSONPlaceholder real. Solicitudes registradas en la red:

| Operación | Solicitud | Estado | Resultado en la interfaz |
|-----------|-----------|--------|--------------------------|
| GET | `GET /users` | 200 | 10 usuarios en la tabla |
| POST | `POST /users` con los 5 campos | 201 | «Usuario creado. ID recibido: 11.» y fila al inicio |
| PUT | `PUT /users/1` con el teléfono cambiado | 200 | Fila de Leanne Graham actualizada |
| PATCH | `PATCH /users/2` con `{"email": "antonette@correo-nuevo.com"}` | 200 | Solo cambia el correo de Ervin Howell |
| DELETE | `DELETE /users/3` tras confirmar | 200 | Clementine Bauch sale de la lista local |
| GET por id | `GET /users/5` | 200 | Detalle de Chelsey Dietrich |
| Error controlado | `GET /users` respondido con 404 | 404 | «No se pudo obtener la lista de usuarios.» y sin «Cargando...» |

**Error controlado (sección 20):** la guía pide cambiar temporalmente la URL del servicio. En vez
de editar el código, la prueba hizo que el navegador respondiera 404 a `GET /users` con
`page.route()` de Playwright. El efecto es el mismo y no queda una URL rota olvidada en el
servicio.

## Evidencias

En [`evidencias/`](evidencias/):

| Archivo | Qué muestra |
|---------|-------------|
| `01_ng_version.png` | `ng version` con Angular 22.2.6 |
| `02_get_lista.png` | GET: tabla con los usuarios de la API |
| `03_post_creado.png` | POST: mensaje con el ID recibido |
| `04_put_editando.png` / `05_put_actualizado.png` | PUT: formulario cargado con `patchValue()` y fila actualizada |
| `06_patch_correo.png` | PATCH: solo cambió el correo |
| `07_delete.png` | DELETE: la fila desapareció |
| `08_busqueda_local.png` | Búsqueda local por «biz» |
| `09_detalle_por_id.png` | Ruta `/usuarios/5` con `obtenerPorId()` |
| `10_error_404.png` | Error controlado con 404 |
| `11_red_solicitudes.png` | Método, estado, URL y cuerpo de cada solicitud (equivale a la pestaña Network de la sección 19) |
| `12_consola_interceptor.png` | Registro del interceptor en la consola |

Las capturas 11 y 12 reproducen lo que Playwright leyó de la red y de la consola del navegador
durante la prueba.

## Actividades complementarias (sección 22)

La guía pide al menos dos. Se hicieron las siete:

| Actividad | Cómo quedó |
|-----------|------------|
| Búsqueda local | `busqueda` es un `signal` y `usuariosFiltrados` un `computed` que filtra por nombre o correo sin pedir nada a la API. La cabecera de la tabla dice «3 de 10 registro(s)» |
| Ruta `/usuarios/:id` | `UsuarioDetalle` recibe `id` como `input()` gracias a `withComponentInputBinding()` y llama a `obtenerPorId()`. Un 404 muestra «No existe un usuario con ID 999.» |
| Contador de solicitudes exitosas | `ContadorSolicitudesService` con un `signal` que alimenta el interceptor y se muestra en la cabecera |
| Botón de PATCH | «Cambiar correo» envía solo `{ email }` con `actualizarParcial()` |
| `apiUrl` en un entorno | `environment.apiUrl`. El servicio solo agrega `/users` |
| Interceptor funcional | `registroInterceptor` escribe `[HTTP] GET .../users -> 200 (289 ms)` o un `warn` si falla |
| Prueba unitaria del servicio | `usuario.service.spec.ts` con `provideHttpClientTesting()` y `HttpTestingController` |

## Pruebas unitarias (`npm test`: 21 en verde, sin Internet)

| Archivo | Qué cubre |
|---------|-----------|
| `usuario.service.spec.ts` | Método, URL y cuerpo de las seis operaciones, y propagación del 404 |
| `usuarios.spec.ts` | Carga y estado «Consultando...», error sin carga infinita, formulario inválido, POST, PUT, PATCH, DELETE y búsqueda |
| `registro-interceptor.spec.ts` | Log y contador en éxito; `warn` y sin contar en error |
| `app.spec.ts` | Cabecera con el contador, `/usuarios/:id`, `/usuarios/999` y redirección de `**` |

Todas usan `HttpTestingController`: no salen a Internet y verifican con `http.verify()` que no
quede ninguna solicitud sin atender.

## Conexión con Spring Boot (anexo de la sección 23)

La misma arquitectura (servicio + `HttpClient` + interceptor + `environment.apiUrl`) es la que usa
el frontend del proyecto final contra la API Spring Boot en `http://localhost:8080/api`. Ahí el
interceptor agrega el token JWT, y el backend habilita CORS para `http://localhost:4200` con una
configuración global en vez de `@CrossOrigin` en cada controlador.

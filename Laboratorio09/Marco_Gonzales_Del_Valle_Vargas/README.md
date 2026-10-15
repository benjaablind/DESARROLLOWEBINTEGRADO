# Laboratorio 09 — Marco Antonio Gonzales del Valle Vargas

**Autenticación con JWT en Spring Boot.** De HTTP Basic a una API REST *stateless* con
tokens firmados, según la Guía de Laboratorio 09.

En la semana 8 cada petición enviaba usuario y contraseña. Ahora el cliente se autentica
**una sola vez** en `POST /api/auth/login`, recibe un JWT firmado y en las peticiones
siguientes solo presenta `Authorization: Bearer <token>`. El servidor ya no necesita la
contraseña ni una sesión: verifica la firma, la vigencia y el emisor del token, y reconstruye
los roles desde sus *claims*.

```
1) POST /api/auth/login {username, password}
2) AuthenticationManager → CustomUserDetailsService (MySQL) + BCrypt
3) TokenService firma un JWT HS256: sub, iss, iat, exp, roles
4) Cliente → Authorization: Bearer <token>
5) OAuth2 Resource Server verifica firma, exp e iss → roles a GrantedAuthority
6) SecurityFilterChain aplica la misma política por roles de la semana 8
```

## Tecnologías

- Java 25 · Spring Boot 4.1.1 · Spring Security 7.1
- `spring-boot-starter-security-oauth2-resource-server` (Nimbus JOSE + JWT)
- `spring-boot-starter-validation` para el request de login
- Spring Data JPA / Hibernate · MySQL 8 · BCrypt

## Qué cambió respecto de la semana 8

| Semana 8 | Semana 9 |
|----------|----------|
| HTTP Basic en cada petición | Login una vez → JWT en `Authorization: Bearer` |
| El servidor valida contraseña por petición | El servidor valida firma, `exp` e `iss` por petición |
| `httpBasic()` | `oauth2ResourceServer().jwt()` |

**Se reutiliza sin cambios:** `Usuario`, `Rol`, sus repositorios y tablas,
`CustomUserDetailsService`, `DatosSeguridadIniciales`, `PublicoController`, BCrypt y toda la
política de autorización por rol. Eso confirma la idea de la guía: la capa de seguridad
evolucionó sin rehacer el dominio de productos.

```
src/main/java/com/utp/productosapi
├── config
│   ├── JwtConfig.java          → SecretKey, JwtEncoder, JwtDecoder (HS256 + issuer)  NUEVO
│   └── SecurityConfig.java     → AuthenticationManager + Resource Server             CAMBIA
├── security
│   └── TokenService.java       → emite el JWT                                        NUEVO
├── dto
│   ├── LoginRequest.java       → record con @NotBlank                                NUEVO
│   ├── TokenResponse.java      → tokenType, accessToken, expiresInSeconds            NUEVO
│   └── ReporteResponse.java    → reto 15.1                                           NUEVO
└── controller
    ├── AuthController.java     → POST /login, GET /me                                NUEVO
    └── AdminController.java    → GET /api/admin/reporte (reto 15.1)                  NUEVO
```

## Cómo ejecutarlo

La clave HS256 **no está en el código ni en `application.properties`**: se lee de la variable
de entorno `JWT_SECRET_BASE64`. Quien conozca esa clave puede fabricar tokens válidos.

**Windows (lo más simple):** desde la carpeta del proyecto,

```powershell
.\iniciar.ps1
```

[iniciar.ps1](iniciar.ps1) genera 32 bytes aleatorios con un generador criptográfico, los deja
en la variable **solo para esa terminal** y arranca la API. Equivale a los comandos de la
sección 4.1 de la guía:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
export JWT_SECRET_BASE64="$(openssl rand -base64 32)"
./mvnw spring-boot:run
```

Si falta la variable, la aplicación **no arranca** — comprobado:

```
Could not resolve placeholder 'JWT_SECRET_BASE64' in value "${JWT_SECRET_BASE64}"
```

Cada arranque con otra clave invalida los tokens anteriores; basta con volver a hacer login.

### Postman

Importar [postman/Laboratorio09_JWT.postman_collection.json](postman/Laboratorio09_JWT.postman_collection.json)
y ejecutar primero **02a** y **02b**: guardan solos el token en `tokenUsuario` y `tokenAdmin`,
así que no hace falta copiarlo a mano. La petición **09** altera la firma automáticamente.

## Evidencia: matriz de pruebas de la sección 11

Ejecutada contra la aplicación levantada con una clave aleatoria:

| N° | Solicitud | Token | Esperado | Obtenido |
|----|-----------|-------|----------|----------|
| 01 | `GET /api/publico/estado` | Ninguno | 200 | **200** `{"estado":"API disponible"}` |
| 02 | `POST /api/auth/login` válido | Ninguno | 200 + JWT | **200** `tokenType: Bearer`, `expiresInSeconds: 1800` |
| 03 | `POST /api/auth/login` clave errónea | Ninguno | 401 | **401** `{"error":"Credenciales invalidas"}` |
| 04 | `GET /api/productos` | Ninguno | 401 | **401** |
| 05 | `GET /api/productos` | USER | 200 | **200** |
| 06 | `POST /api/productos` | USER | 403 | **403** |
| 07 | `POST /api/productos` | ADMIN | 200/201 | **201** |
| 08 | `GET /api/auth/me` | ADMIN | 200 + roles | **200** `{"roles":["ROLE_USER","ROLE_ADMIN"],"username":"admin"}` |
| 09 | `GET /api/productos` | JWT modificado | 401 | **401** |
| 10 | `GET /api/productos` | JWT vencido | 401 | **401** (prueba automatizada) |

Con el token alterado (fila 09), el servidor explica el rechazo en el encabezado estándar:

```
WWW-Authenticate: Bearer error="invalid_token",
  error_description="... Signed JWT rejected: Invalid signature"
```

### Reto de aplicación 15.1 — `GET /api/admin/reporte`

| Token | Obtenido |
|-------|----------|
| Ninguno | **401** |
| USER | **403** |
| ADMIN | **200** `{"fechaServidor":"2026-10-15T10:16:19.97","totalProductos":1}` |

Conserva Controller → Service → Repository: `AdminController` llama a
`ProductoService.generarReporte()`, que usa `productoRepository.count()`.

### Inspección del JWT (sección 11.3)

Header y payload del token de `admin`, decodificados localmente (Base64URL, sin enviarlos a
ningún sitio web):

```
header : {"alg": "HS256"}
payload: {"iss": "productos-api", "sub": "admin", "iat": 1792077379,
          "exp": 1792079179, "roles": ["ROLE_ADMIN", "ROLE_USER"]}
```

`exp − iat = 1800` s = 30 minutos. El payload se lee sin ninguna clave: **un JWT firmado no
está cifrado**. Por eso solo lleva identidad y roles, nunca la contraseña.

## Pruebas automatizadas

```bash
./mvnw test
```

Resultado: `Tests run: 39, Failures: 0, Errors: 0` — `BUILD SUCCESS`.

| Clase | Pruebas | Qué verifica |
|-------|---------|--------------|
| `TokenServiceTest` | 4 | Sección 12 de la guía: `sub`, `iss`, `roles` y `exp` sobreviven a codificar/decodificar; solo se incluyen autoridades `ROLE_`; firma alterada y token de otra clave se rechazan |
| `JwtFlujoCompletoTest` | 15 | **Las 10 filas de la matriz**, de punta a punta: login real con BCrypt, token firmado de verdad, token vencido, token de otro emisor, login con campos vacíos (400) y el reto 15.1 |
| `ProductoControllerSecurityTest` | 5 | Reglas por rol sobre productos con identidad JWT |
| `InventarioControllerSecurityTest` | 4 | Reglas por rol sobre inventario con identidad JWT |
| `ProductoRepositoryTest` | 5 | Consultas JPQL (semana 7) |
| `ProductoServiceTransaccionalTest` | 6 | Commit y rollback (semana 7) |

La fila 10 (token vencido) se prueba firmando un token que venció hace 5 minutos, más allá de
los 60 s de tolerancia de reloj que aplica el decodificador. Así no hace falta esperar como
en la prueba manual de la sección 11.2.

Las pruebas usan una clave **exclusiva de test** declarada en
`src/test/resources/application.properties`: es una cadena pública que no protege nada real,
y la aplicación de verdad sigue leyendo su clave del entorno.

## Correcciones respecto del código de la guía

1. **`TokenServiceTest`, sección 12:** la guía hace `decoded.getIssuer().toString()`. Eso
   **falla** con `IllegalArgumentException: Unable to convert claim 'iss' ... to URL`, porque
   `getIssuer()` intenta convertir el emisor a `URL` y `"tienda-api"` no lo es. Se usa
   `decoded.getClaimAsString(JwtClaimNames.ISS)`, que lo lee como texto. El servidor no tiene
   este problema: el validador de emisor compara cadenas.
2. **Firma alterada en las pruebas:** se modifica el *primer* carácter de la firma, no el
   último. En una firma HS256 de 32 bytes el último carácter Base64URL lleva bits de relleno, y
   alterarlo puede no cambiar la firma decodificada, lo que daría una prueba que pasa o falla
   según el token.
3. **Dependencias de prueba de Boot 4:** como en la semana 8, se usan
   `spring-boot-starter-security-test` y `spring-boot-starter-webmvc-test`. Además, los
   `@WebMvcTest` necesitan `@MockitoBean` de `UserDetailsService` y `JwtDecoder`, porque
   `SecurityConfig` ahora depende de ellos — es el ajuste que la guía anticipa en la nota de la
   sección 14 de la semana 8.

## Decisiones propias

- **Emisor `productos-api`** en lugar de `tienda-api`, coherente con el nombre del proyecto que
  vengo arrastrando desde la semana 6.
- **Login con campos vacíos → 400** con un mensaje que nombra los campos, mediante un manejador
  de `MethodArgumentNotValidException` en `ApiExceptionHandler`.
- **Mismo mensaje para usuario inexistente y contraseña incorrecta** (`Credenciales invalidas`),
  para no revelar qué usuarios existen.

## Nota sobre la verificación

En esta máquina **no hay MySQL Server instalado**, así que la matriz se obtuvo levantando la
aplicación contra una base en memoria, en una copia temporal fuera del proyecto, con una clave
JWT aleatoria real. El código entregado apunta a MySQL.

La breve explicación y la extensión opcional (sección 14) están en
[RESPUESTAS_REFLEXION.md](RESPUESTAS_REFLEXION.md).

# Laboratorio 08 — Marco Antonio Gonzales del Valle Vargas

**Seguridad con Spring Security: roles y permisos.** Autenticación, autorización y control
de acceso sobre la API de productos e inventario, según la Guía de Laboratorio 08.

Hasta la semana 7 cualquiera podía crear o borrar productos. Ahora, antes de que una
petición llegue al Controller, pasa por la cadena de filtros de Spring Security, que
responde dos preguntas en orden:

1. **¿Quién eres?** (autenticación): usuario y contraseña contra MySQL, con hash BCrypt.
2. **¿Qué puedes hacer?** (autorización): el rol decide si la operación se permite.

Esta semana las credenciales viajan con **HTTP Basic**; en la semana 9 se reemplaza por JWT
conservando los mismos usuarios y roles.

## Tecnologías

- Java 25
- Spring Boot 4.1.1 · Spring Security 7.1
- Spring Data JPA / Hibernate · MySQL 8
- BCrypt (`BCryptPasswordEncoder`)
- JUnit 5 + MockMvc + `@WithMockUser`

## Qué se agregó respecto de la semana 7

```
src/main/java/com/utp/productosapi
├── config
│   ├── SecurityConfig.java            → SecurityFilterChain + PasswordEncoder (BCrypt)
│   ├── DatosSeguridadIniciales.java   → crea roles y usuarios de laboratorio
│   └── MethodSecurityConfig.java      → @EnableMethodSecurity (reto avanzado)
├── model
│   ├── Usuario.java                   → username, password (hash), activo, Set<Rol>
│   └── Rol.java                       → ROLE_USER / ROLE_ADMIN
├── repository
│   ├── UsuarioRepository.java         → findByUsername
│   └── RolRepository.java             → findByNombre
├── security
│   └── CustomUserDetailsService.java  → Usuario de MySQL → UserDetails
└── controller
    ├── PublicoController.java         → GET /api/publico/estado (sin credenciales)
    └── InventarioController.java      → actividad de consolidación
```

Las clases de las semanas 6 y 7 (productos, movimientos, JPQL, transacciones) se conservan.

## Política de acceso

| Recurso | Método | Acceso |
|---------|--------|--------|
| `/api/publico/**` | GET | Público |
| `/api/productos/**` | GET | `ROLE_USER` o `ROLE_ADMIN` |
| `/api/productos/**` | POST / PUT / PATCH / DELETE | `ROLE_ADMIN` |
| `/api/inventario/**` | GET | `ROLE_USER` o `ROLE_ADMIN` |
| `/api/inventario/ajustes` | POST | `ROLE_ADMIN` |
| Cualquier otro recurso | — | Usuario autenticado |

Las reglas están en [SecurityConfig.java](src/main/java/com/utp/productosapi/config/SecurityConfig.java),
de la más específica a la más general, terminando en `anyRequest().authenticated()`.

## Usuarios de laboratorio

| Usuario | Contraseña | Roles |
|---------|------------|-------|
| `usuario` | `Usuario123*` | `ROLE_USER` |
| `admin` | `Admin123*` | `ROLE_ADMIN`, `ROLE_USER` |

Se crean al arrancar **solo si no existen**, con la contraseña ya codificada por BCrypt. Son
credenciales conocidas a propósito para que el laboratorio sea reproducible; en un sistema
real se aprovisionan con secretos, nunca desde el repositorio.

## Cómo ejecutarlo

1. Con la base `productos_db` de semanas anteriores y la contraseña de MySQL en
   `application.properties`:

   ```bash
   ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
   ```

2. Verificar las tablas de seguridad con [sql/04_verificar_seguridad.sql](sql/04_verificar_seguridad.sql):
   la columna `password` debe mostrar hashes `$2a$10$...`, nunca las contraseñas.

3. Importar [postman/Laboratorio08_Seguridad_Roles.postman_collection.json](postman/Laboratorio08_Seguridad_Roles.postman_collection.json):
   cada petición ya trae configurada su autenticación Basic.

## Evidencia: matriz de pruebas de la sección 13

Ejecutada contra la aplicación levantada:

| # | Credenciales | Solicitud | Esperado | Obtenido |
|---|--------------|-----------|----------|----------|
| 1 | Ninguna | `GET /api/publico/estado` | 200 | **200** `{"estado":"API disponible"}` |
| 2 | Ninguna | `GET /api/productos` | 401 | **401** |
| 3 | usuario | `GET /api/productos` | 200 | **200** |
| 4 | usuario | `POST /api/productos` | 403 | **403** |
| 5 | admin | `POST /api/productos` | 201/200 | **201** |
| 6 | admin | `DELETE /api/productos/1` | 204/200 | **204** |
| 7 | admin + clave errónea | `GET /api/productos` | 401 | **401** |

### Actividad de consolidación (sección 17)

| Credenciales | Solicitud | Obtenido |
|--------------|-----------|----------|
| Ninguna | `GET /api/inventario` | **401** |
| usuario | `GET /api/inventario` | **200** |
| usuario | `POST /api/inventario/ajustes?productoId=2&tipo=ENTRADA&cantidad=5` | **403** |
| admin | `POST /api/inventario/ajustes?productoId=2&tipo=ENTRADA&cantidad=5` | **200** (stock 3 → 8) |
| Ninguna | `GET /api/otro-recurso` (no declarado) | **401** |
| usuario | `GET /api/otro-recurso` (no declarado) | **404**: pasa la seguridad, no existe la ruta |

La última fila muestra el punto 4 de la actividad: un recurso no declarado exige estar
autenticado, y una vez autenticado responde lo que corresponda a la ruta.

## Pruebas automatizadas

```bash
./mvnw test
```

Resultado: `Tests run: 20, Failures: 0, Errors: 0` — `BUILD SUCCESS`.

| Clase | Pruebas | Qué verifica |
|-------|---------|--------------|
| `ProductoControllerSecurityTest` | 5 | 401 sin credenciales, 200 USER, 403 USER en POST y DELETE, 204 ADMIN |
| `InventarioControllerSecurityTest` | 4 | 401, 200 USER, **403 USER en ajustes**, 200 ADMIN *(actividad, punto 5)* |
| `ProductoRepositoryTest` | 5 | Consultas JPQL de la semana 7 |
| `ProductoServiceTransaccionalTest` | 6 | Commit y rollback de la semana 7 |

Las pruebas de seguridad usan `@WebMvcTest` con `@Import(SecurityConfig.class)`: el slice no
carga las clases `@Configuration` por sí solo, y sin importarla se estarían probando las
reglas por defecto de Spring Boot, no las del laboratorio.

### Nota sobre las dependencias de prueba (Spring Boot 4)

La guía indica `spring-security-test`. En Spring Boot 4 eso no basta para `@WebMvcTest`: el
slice arranca sin la auto-configuración de seguridad y falla con
*No qualifying bean of type HttpSecurity*. Se usa en su lugar
**`spring-boot-starter-security-test`**, que incluye `spring-security-test` y además registra
la seguridad dentro del slice. También se agregó `spring-boot-starter-webmvc-test`, el módulo
de Boot 4 donde vive `@WebMvcTest`. Es el mismo patrón que en la semana 6 con
`spring-boot-starter-data-jpa-test`.

## Reto avanzado (sección 18): seguridad a nivel de método

Implementado. [MethodSecurityConfig.java](src/main/java/com/utp/productosapi/config/MethodSecurityConfig.java)
activa `@EnableMethodSecurity`, y `ProductoService.eliminar()` lleva
`@PreAuthorize("hasRole('ADMIN')")`.

Es una segunda barrera, no un reemplazo: la regla por URL ya bloquea el `DELETE` de un USER
con 403 antes de llegar al Controller. La anotación protege la operación de negocio en sí,
de modo que si mañana otro endpoint u otro servicio invoca `eliminar()`, sigue exigiendo
ROLE_ADMIN aunque nadie se acuerde de agregar la regla HTTP.

## Nota sobre la verificación

En esta máquina **no hay MySQL Server instalado**, así que la matriz de arriba se obtuvo
levantando la aplicación contra una base en memoria, en una copia temporal fuera del
proyecto. El código entregado apunta a MySQL.

Sobre las contraseñas en BCrypt: `SecurityConfig` registra un `BCryptPasswordEncoder` puro
(no un `DelegatingPasswordEncoder`), así que el login de `usuario` y `admin` solo puede
funcionar si lo guardado en `usuarios.password` es un hash BCrypt válido; una contraseña en
texto plano daría 401. La captura de la tabla con los hashes `$2a$...` queda pendiente de
tomarse en Workbench con [sql/04_verificar_seguridad.sql](sql/04_verificar_seguridad.sql).

Las preguntas de reflexión están en [RESPUESTAS_REFLEXION.md](RESPUESTAS_REFLEXION.md).

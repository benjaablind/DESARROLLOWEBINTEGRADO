# Laboratorio 10 — Marco Antonio Gonzales del Valle Vargas

**Integración del back-end seguro con Spring Boot.** JPA + JPQL + transacciones + Spring
Security + JWT, funcionando juntos como una sola API, según la Guía de Laboratorio 10.

Esta semana no agrega una tecnología nueva: se toma el proyecto de las semanas 6 a 9 (el mismo
del Laboratorio 09) y se demuestra que sus piezas funcionan juntas. Para eso se agregaron una
ruta JPQL de rango, una salida de inventario con rollback comprobable, un resumen para el
administrador y la matriz de pruebas 200/201, 400, 401, 403, 404 y 409.

## 1. Objetivo

Demostrar un back-end RESTful **persistente** (MySQL), con **consultas JPQL**, **transacciones
confiables**, **usuarios y roles** con BCrypt y **autenticación JWT**, respaldado por pruebas
automatizadas y empaquetado como JAR.

## 2. Arquitectura

```
Cliente / Postman
   │  Authorization: Bearer <JWT>
   ▼
SecurityFilterChain + JwtDecoder        firma, exp e iss -> 401 si fallan
   │  roles del token -> ROLE_USER / ROLE_ADMIN  -> 403 si no alcanzan
   ▼
@RestController                         solo traduce HTTP (sin logica de autenticacion)
   ▼
@Service  (@Transactional)              reglas de negocio y frontera de la transaccion
   ▼
JpaRepository + consultas derivadas / JPQL
   ▼
Hibernate / JPA  ->  MySQL (productos_db)
```

| Capa | Clases |
|------|--------|
| Seguridad | `SecurityConfig`, `JwtConfig`, `TokenService`, `CustomUserDetailsService` |
| Controller | `AuthController`, `ProductoController`, `InventarioController`, `AdminController` |
| Service | `ProductoService` (CRUD, JPQL, inventario), `AdminResumenService` (nuevo) |
| Repository | `ProductoRepository`, `MovimientoStockRepository`, `UsuarioRepository`, `RolRepository` |
| Errores | `ApiExceptionHandler`, `RecursoNoEncontradoException` (404), `ReglaNegocioException` (400), `StockInsuficienteException` (409, nueva) |

## 3. Requisitos

| Herramienta | Versión usada |
|-------------|---------------|
| Java | 25 (Temurin 25.0.4) |
| Spring Boot / Spring Security | 4.1.1 / 7.1 |
| Maven | el wrapper del proyecto (`mvnw.cmd`); no hace falta instalarlo |
| MySQL | MariaDB 10.4 de XAMPP (compatible con MySQL) |
| Postman o Newman | para la colección de `postman/` |

## 4. Variables de entorno

Ningún secreto está en Git. `application.properties` solo los referencia:

| Variable | Para qué | Valor |
|----------|----------|-------|
| `DB_PASSWORD` | Contraseña de MySQL (`root`) | Con XAMPP, vacía |
| `JWT_SECRET_BASE64` | Clave HS256 que firma y verifica los tokens | 32 bytes aleatorios en Base64 |
| `DB_USERNAME` | Opcional; por defecto `root` | — |

`iniciar.ps1` pide la contraseña de MySQL sin mostrarla y genera la clave JWT solo para esa
terminal.

## 5. Base de datos

```powershell
C:\xampp\mysql\bin\mysql.exe -u root -e "source sql/01_crear_base_datos.sql"
```

Las tablas no se crean a mano: Hibernate genera `productos`, `movimientos_stock`, `usuarios`,
`roles` y `usuarios_roles` al arrancar. `DatosSeguridadIniciales` crea los roles y los dos
usuarios. Para tener productos de ejemplo, con la API ya arrancada:

```powershell
C:\xampp\mysql\bin\mysql.exe -u root -e "source sql/03_datos_prueba.sql"
```

| Script | Uso |
|--------|-----|
| `sql/01_crear_base_datos.sql` | Crea `productos_db` |
| `sql/02_verificar_tablas.sql` | Tablas y contenido |
| `sql/03_datos_prueba.sql` | 5 productos de ejemplo |
| `sql/04_verificar_seguridad.sql` | Usuarios con hash BCrypt y sus roles |
| `sql/05_evidencia_transaccion.sql` | Stock y movimientos antes y después de una salida (nuevo) |

## 6. Ejecución

```powershell
.\mvnw.cmd clean test        # 56 pruebas
.\mvnw.cmd clean package     # target\productos-api-0.0.1-SNAPSHOT.jar
.\iniciar.ps1                # con Maven
.\iniciar.ps1 -Jar           # con el JAR empaquetado
```

La API queda en `http://localhost:8080`.

## 7. Endpoints

| Operación | Método y ruta | Acceso |
|-----------|---------------|--------|
| Iniciar sesión | `POST /api/auth/login` | Público |
| Consultar perfil | `GET /api/auth/me` | JWT válido |
| Listar productos | `GET /api/productos` | USER o ADMIN |
| Buscar por rango (JPQL) | `GET /api/productos/rango?min=&max=` | USER o ADMIN |
| Crear / actualizar / eliminar producto | `POST` / `PUT` / `DELETE /api/productos/**` | ADMIN |
| Consultar inventario | `GET /api/inventario/**` | USER o ADMIN |
| Registrar salida | `POST /api/inventario/{id}/salida?cantidad=` | ADMIN |
| Ajustar inventario | `POST /api/inventario/ajustes` | ADMIN |
| Resumen administrativo | `GET /api/admin/resumen` | ADMIN |
| Reporte (semana 9) | `GET /api/admin/reporte` | ADMIN |

`/api/productos/precio` se conserva como alias de `/rango` para no romper a los clientes de la
semana 7.

## 8. Usuarios y roles de prueba

| Usuario | Roles |
|---------|-------|
| `usuario` | `ROLE_USER` |
| `admin` | `ROLE_ADMIN`, `ROLE_USER` |

Son credenciales conocidas a propósito, de laboratorio; están en `DatosSeguridadIniciales` y en
la colección de Postman. En MySQL solo se guarda el hash BCrypt (`$2a$10$...`).

## 9. Pruebas

### Automatizadas: `mvnw clean test` → 56 pruebas, 0 fallas

| Clase | Pruebas | Qué cubre |
|-------|---------|-----------|
| `IntegracionSemana10Test` (nueva) | 14 | La matriz completa de la sección 13 (E01–E12) de punta a punta, más el 400 del rango y el 403 de la salida con USER |
| `AdminControllerSecurityTest` (nueva, sección 14) | 3 | Resumen: 200 con ADMIN, 403 con USER y 401 sin token, con `jwt()` de Spring Security Test |
| `JwtFlujoCompletoTest` | 15 | Login, token alterado, vencido y de otro emisor (semana 9) |
| `ProductoServiceTransaccionalTest` | 6 | Commit y rollback del servicio (semana 7) |
| Otras de semanas anteriores | 18 | Repositorio JPQL, seguridad de productos e inventario, `TokenService` |

### Matriz de integración (sección 13)

Ejecutada con Postman (`postman/Laboratorio10_Integracion.postman_collection.json`) contra el JAR
y MySQL de XAMPP: **21 peticiones, 32 verificaciones, 0 fallas**.

| N.° | Escenario | Rol | Esperado | Obtenido |
|-----|-----------|-----|----------|----------|
| 1 | Login USER válido | usuario + password | 200 + accessToken | 200 + accessToken |
| 2 | `GET /api/productos` sin token | Ninguno | 401 | 401 |
| 3 | `GET /api/productos` | ROLE_USER | 200 | 200 |
| 4 | `POST /api/productos` | ROLE_USER | 403 | 403 |
| 5 | `POST /api/productos` | ROLE_ADMIN | 200/201 | 201 |
| 6 | `GET` rango JPQL | ROLE_USER | 200 + datos filtrados | 200, solo precios entre 100 y 1000, ordenados |
| 7 | `POST` salida válida | ROLE_ADMIN | 200 + stock disminuye | 200, stock − 2 |
| 8 | `POST` salida con stock insuficiente | ROLE_ADMIN | 409 + rollback | 409, stock y movimientos sin cambios |
| 9 | `GET /api/admin/resumen` | ROLE_USER | 403 | 403 |
| 10 | `GET /api/admin/resumen` | ROLE_ADMIN | 200 | 200 |
| 11 | Token alterado | JWT firma inválida | 401 | 401 |
| 12 | Producto inexistente | ROLE_ADMIN | 404 | 404 |

### Quality gate (sección 15)

| Control | Evidencia | Resultado |
|---------|-----------|-----------|
| Compilación | `mvnw clean test` | BUILD SUCCESS, 56/56 |
| Empaquetado | `mvnw clean package` | `target/productos-api-0.0.1-SNAPSHOT.jar` (57 MB) |
| Persistencia | Tablas en MySQL | 5 tablas creadas por Hibernate |
| JPQL | `GET /rango` | Filtrado y ordenado correctamente |
| Transacción | Salida válida y fallida | Commit en el éxito; rollback en el error |
| Autenticación | `POST /api/auth/login` | Token solo con credenciales válidas |
| Autorización | Matriz 401/403/200 | Reglas por rol aplicadas |
| Secretos | Revisión de archivos | Sin contraseña ni clave JWT versionadas |
| Documentación | Este README + Postman | Reproducible por un tercero |

## 10. Evidencias de transacción y seguridad

Ejecución del 29/10/2026 con el JAR contra MySQL de XAMPP (producto 1, *Laptop Lenovo
ThinkPad*, stock inicial 8). El token se muestra recortado, como pide la guía.

**JPQL (sección 12.1).** `GET /api/productos/rango?min=100&max=1000` con USER devolvió solo el
mouse (S/ 320) y la silla (S/ 850). Hibernate tradujo la consulta sobre la entidad `Producto`
a SQL sobre la tabla `productos`:

```sql
select p1_0.id, p1_0.categoria, p1_0.nombre, p1_0.precio, p1_0.stock
from productos p1_0
where p1_0.precio between ? and ?
order by p1_0.precio
```

**Commit y rollback (secciones 12.2 y 12.3),** comprobados en la base con
`sql/05_evidencia_transaccion.sql`:

| Paso | Respuesta | `stock` | Movimientos del producto |
|------|-----------|---------|--------------------------|
| Estado inicial | — | 8 | 0 |
| `POST /api/inventario/1/salida?cantidad=2` | 200 | **6** | **1** (SALIDA, 2) |
| `POST /api/inventario/1/salida?cantidad=99999` | **409** `Stock insuficiente. Stock actual: 6, solicitado: 99999` | **6** | **1** |

La salida fallida no dejó ningún cambio parcial: ni descontó stock ni insertó un movimiento.

**Resumen administrativo (sección 11.4).** Con ADMIN:
`{"totalProductos":5,"productosConStockBajo":1,"totalMovimientos":1,"estado":"BACKEND_OPERATIVO"}`.
Con USER: 403.

## Decisiones propias

- **Se reutilizó `registrarSalida()` de la semana 7,** como indica la guía, en lugar de crear un
  `InventarioService` con un método duplicado. Ya cumplía las cinco reglas de la sección 3.2:
  buscar el producto, validar la cantidad, validar el stock, descontar y registrar el movimiento
  en una sola transacción.
- **409 para el stock insuficiente:** `StockInsuficienteException` extiende
  `ReglaNegocioException`, así que el código que ya la trataba como regla de negocio sigue igual.
  Una cantidad no positiva sigue siendo 400, porque es un dato inválido y no un conflicto.
- **400 explícito para parámetros faltantes o mal tipados.** Sin ese manejador, el reenvío
  interno a `/error` pasa por la cadena de seguridad sin token y la respuesta termina siendo un
  401 engañoso.
- **Toda escritura en `/api/inventario/**` es de ADMIN.** Antes solo lo era `/ajustes`; la nueva
  ruta de salida queda cubierta por la misma regla, sin depender de recordarla una por una.
- **Se conservó `productos_db`** (la base de las semanas 6 a 9) en lugar del `tienda_db` del
  ejemplo de la guía: la consigna es estabilizar el proyecto existente, no crear otro.

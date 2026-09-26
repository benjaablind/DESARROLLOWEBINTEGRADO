# Laboratorio 07 — Marco Antonio Gonzales del Valle Vargas

**JPQL y transacciones con Spring Boot.** Consultas personalizadas y operaciones atómicas
sobre la API de productos, según la Guía de Laboratorio 07.

La semana 6 dejó el CRUD persistido en MySQL. Aquí se agregan dos cosas:

1. **Consultas que `JpaRepository` no cubre**: búsqueda parcial por nombre, filtro por
   categoría, rango de precios y stock bajo, resueltas con consultas derivadas y JPQL.
2. **Operaciones de negocio atómicas**: cada salida de inventario descuenta stock *y*
   registra un `MovimientoStock` dentro de la misma transacción — o no hace ninguna de las
   dos cosas.

## Tecnologías

- Java 25
- Spring Boot 4.1.1 (Spring Web, Spring Data JPA, Starter Test, Starter Data JPA Test)
- Hibernate como implementación ORM
- MySQL 8 (driver `com.mysql:mysql-connector-j`)
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- H2 en memoria, **solo con alcance `test`**

## Estructura

```
src/main/java/com/utp/productosapi
├── ProductosApiApplication.java
├── controller
│   └── ProductoController.java          → HTTP, sin @Transactional
├── service
│   └── ProductoService.java             → frontera transaccional y reglas de negocio
├── repository
│   ├── ProductoRepository.java          → consulta derivada + 3 consultas JPQL
│   └── MovimientoStockRepository.java   → JPQL navegando la relación
├── model
│   ├── Producto.java                    → precio ahora es BigDecimal
│   └── MovimientoStock.java             → @ManyToOne hacia Producto
└── exception
    ├── RecursoNoEncontradoException.java   → 404
    ├── ReglaNegocioException.java          → 400
    └── ApiExceptionHandler.java            → @RestControllerAdvice

sql/     → crear BD, verificar tablas, datos de prueba, comprobar commit/rollback
postman/ → colección con las consultas JPQL y las pruebas de transacción
```

### Qué cambió respecto de la semana 6

| Semana 6 | Semana 7 |
|----------|----------|
| Solo métodos heredados de `JpaRepository` | Consulta derivada + `@Query` con JPQL |
| `precio` era `double` | `precio` es `BigDecimal(12,2)`, exacto para importes |
| Una sola entidad | Se agrega `MovimientoStock` con `@ManyToOne` |
| Validaciones con `IllegalArgumentException` (500) | `ReglaNegocioException` → 400, `RecursoNoEncontradoException` → 404 |
| `actualizar()` llamaba a `save()` | Se apoya en *dirty checking* dentro de la transacción |

Se conservó el paquete `com.utp.productosapi` y la base `productos_db` de la semana 6, en
lugar del `com.utp.tienda` / `tienda_db` que usa la guía como ejemplo: la propia guía indica
partir del proyecto de la semana anterior y adaptar solo lo necesario.

## Cómo ejecutarlo

1. Base de datos (si vienes de la semana 6 ya existe):

   ```sql
   CREATE DATABASE productos_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Poner la contraseña de MySQL en `src/main/resources/application.properties`.

3. Levantar la API:

   ```bash
   ./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
   ```

   Hibernate crea `movimientos_stock` y su clave foránea hacia `productos`.

4. Cargar datos de prueba: `sql/03_datos_prueba.sql` (o los POST del CRUD).

## Endpoints

| Método | URL | Qué hace |
|--------|-----|----------|
| GET | `/api/productos/buscar?texto=lap` | JPQL: nombre parcial, sin distinguir mayúsculas |
| GET | `/api/productos/categoria/{categoria}` | Consulta derivada `findByCategoriaIgnoreCase` |
| GET | `/api/productos/precio?min=300&max=1500` | JPQL: rango de precio, ordenado ascendente |
| GET | `/api/productos/stock-bajo?limite=5` | JPQL: `stock <= limite` *(actividad 1)* |
| POST | `/api/productos/{id}/salidas?cantidad=2` | Transacción: descuenta stock + movimiento SALIDA |
| POST | `/api/productos/{id}/entradas?cantidad=5` | Transacción: suma stock + movimiento ENTRADA *(actividad 3)* |
| POST | `/api/productos/{id}/salidas/simular-error?cantidad=1` | Fuerza rollback |

Se mantienen los endpoints CRUD de la semana 6 (`GET`, `POST`, `PUT`, `PATCH /precio`, `DELETE`).

## Evidencia de ejecución

Todas estas llamadas se ejecutaron contra la aplicación levantada:

| Prueba | Resultado |
|--------|-----------|
| `GET /buscar?texto=lap` | 200 — devuelve solo *Laptop Lenovo ThinkPad* |
| `GET /categoria/tecnologia` (minúsculas) | 200 — los 3 productos de Tecnologia |
| `GET /precio?min=300&max=1500` | 200 — Mouse 320 → Silla 850 → Escritorio 1200 → Monitor 1450 |
| `GET /precio?min=2000&max=100` | **400** — "El precio minimo no puede superar al maximo" |
| `GET /stock-bajo?limite=5` | 200 — Escritorio ejecutivo (stock 4) |
| `GET /buscar?texto=␣␣␣` | **400** — "El texto de busqueda no puede estar vacio" |
| `POST /1/salidas?cantidad=2` | 200 — stock 8 → **6**, movimiento SALIDA registrado |
| `POST /1/salidas/simular-error?cantidad=1` | **500** — y el stock **sigue en 6**: rollback |
| `POST /1/entradas?cantidad=5` | 200 — stock 6 → **11** |
| `POST /1/salidas?cantidad=99` | **400** — "Stock insuficiente. Disponible: 11" |
| `POST /999/salidas?cantidad=1` | **404** — "Producto no encontrado: 999" |

La llamada que falla es la prueba clave: devuelve 500 **y no deja nada a medias**, ni el
descuento de stock ni el movimiento.

## SQL que Hibernate genera desde JPQL

```sql
-- buscarPorNombre(:texto)
select p1_0.id, p1_0.categoria, p1_0.nombre, p1_0.precio, p1_0.stock
from productos p1_0
where lower(p1_0.nombre) like lower(('%'||?||'%')) order by p1_0.nombre

-- buscarPorRangoPrecio(:min, :max)
select p1_0.id, p1_0.categoria, p1_0.nombre, p1_0.precio, p1_0.stock
from productos p1_0 where p1_0.precio between ? and ? order by p1_0.precio

-- registrarSalida(): el UPDATE sale del dirty checking, no de un save()
insert into movimientos_stock (cantidad, fecha, producto_id, tipo, id) values (?,?,?,?,default)
update productos set categoria=?, nombre=?, precio=?, stock=? where id=?
```

JPQL nombra `Producto` y `p.nombre`; Hibernate es quien traduce eso a `productos` y
`p1_0.nombre`. Por eso escribir nombres de tablas dentro de JPQL rompe la consulta al
arrancar.

## Pruebas

```bash
./mvnw test
```

Resultado: `Tests run: 11, Failures: 0, Errors: 0` — `BUILD SUCCESS`.

- **`ProductoRepositoryTest`** (`@DataJpaTest`, 5 pruebas) — verifica las consultas JPQL y la
  derivada: filtro parcial, categoría sin distinguir mayúsculas, rango ordenado y stock bajo.
- **`ProductoServiceTransaccionalTest`** (`@SpringBootTest`, 6 pruebas) — commit, rollback,
  entrada, stock insuficiente y las dos validaciones de negocio.

Esta segunda clase **no** lleva `@Transactional` a propósito: si la prueba corriera dentro de
su propia transacción, todo se revertiría al final y sería imposible distinguir una salida
confirmada de una revertida. La prueba `errorSimuladoDebeRevertirStockYMovimiento` llama al
endpoint de error, comprueba que se lanza `IllegalStateException` y luego relee la base: el
stock sigue en 8 y no hay movimientos.

## Reto avanzado (sección 17.1): `@Modifying`

No se implementó en el flujo principal, tal como permite la guía. La idea sería:

```java
@Modifying
@Query("UPDATE Producto p SET p.precio = p.precio * :factor WHERE p.categoria = :categoria")
int ajustarPrecios(@Param("factor") BigDecimal factor, @Param("categoria") String categoria);
```

El cuidado está en que una actualización masiva JPQL **se ejecuta directamente en la base de
datos y no pasa por el contexto de persistencia**: no dispara el *dirty checking*, no
actualiza las entidades que ya estaban cargadas en memoria y deja el contexto desincronizado
(una entidad ya cargada seguiría mostrando el precio viejo). Por eso suele acompañarse de
`@Modifying(clearAutomatically = true, flushAutomatically = true)`, y conviene ejecutarla al
inicio de la transacción o en un contexto donde no haya entidades cargadas de esa tabla.

## Nota sobre la verificación

En esta máquina **no hay MySQL Server instalado** (el puerto 3306 no responde), así que la
evidencia de arriba se obtuvo levantando la aplicación contra una base en memoria, en una
copia temporal fuera del proyecto. El código entregado apunta a MySQL y los scripts de
`sql/` están listos para reproducir la comprobación de commit/rollback en Workbench:
`04_comprobar_transacciones.sql` indica en qué orden alternar las consultas con las llamadas
de Postman.

Las preguntas de reflexión están respondidas en
[RESPUESTAS_REFLEXION.md](RESPUESTAS_REFLEXION.md).

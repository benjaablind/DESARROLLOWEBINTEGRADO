# Preguntas de reflexión — Laboratorio 07

Marco Antonio Gonzales del Valle Vargas

**1. ¿Por qué JPQL utiliza el nombre de la entidad y no el nombre de la tabla?**
Porque JPQL consulta el modelo de objetos, no el esquema físico. Escribo
`SELECT p FROM Producto p WHERE p.precio BETWEEN :min AND :max` y Hibernate, que conoce el
mapeo (`@Entity`, `@Table`, `@Column`), lo traduce a
`select ... from productos p1_0 where p1_0.precio between ? and ?`. Esa indirección da dos
ventajas: la consulta sigue siendo válida si cambio el nombre físico de la tabla o de una
columna en las anotaciones, y la misma JPQL funciona en MySQL, PostgreSQL o H2 porque cada
dialecto genera su propio SQL. Si escribiera `FROM productos`, la consulta ni siquiera
arranca: Hibernate valida las consultas contra las entidades al iniciar la aplicación y
falla con *Not a managed type*.

**2. ¿Cuándo elegirías una consulta derivada y cuándo @Query?**
La regla que apliqué es usar el nivel más simple que resuelva el caso. Una consulta derivada
como `findByCategoriaIgnoreCase(String categoria)` basta cuando el filtro se expresa
cómodamente en el nombre del método: es declarativa, no hay JPQL que mantener y Spring Data
la valida al arrancar. Paso a `@Query` cuando el nombre del método se volvería ilegible o el
mecanismo derivado no alcanza: mi `buscarPorNombre` necesita `LOWER(...) LIKE
CONCAT('%', :texto, '%')` con un `ORDER BY`, y el rango de precios necesita `BETWEEN` con dos
parámetros y orden ascendente. Un método derivado equivalente sería algo como
`findByNombreContainingIgnoreCaseOrderByNombreAsc`, que ya cuesta más leer que la JPQL. SQL
nativo lo reservaría solo para algo específico del motor.

**3. ¿Por qué la frontera transaccional se ubica normalmente en la capa Service?**
Porque la unidad de trabajo se define por la operación de negocio, no por el acceso a datos
ni por el protocolo. `registrarSalida` toca dos repositorios —descuenta el stock del producto
y guarda el `MovimientoStock`— y ambas cosas tienen sentido solo juntas; si la transacción
estuviera en el Repository, cada `save()` sería su propia transacción y podría quedar el
stock descontado sin movimiento. Ponerla en el Controller tampoco corresponde: mezclaría HTTP
con la lógica y mantendría la transacción abierta durante la serialización de la respuesta.
El Service es la única capa que conoce la regla completa, así que es donde va `@Transactional`.

**4. ¿Qué diferencia existe entre llamar save() y confiar en dirty checking dentro de una transacción?**
Dentro de la transacción, la entidad que devuelve `findById()` queda *administrada* por el
contexto de persistencia: Hibernate guarda una copia de su estado original y, al hacer commit,
compara y emite el `UPDATE` solo si algo cambió. Eso es dirty checking, y por eso en
`registrarSalida` hago `producto.setStock(...)` sin llamar a `save()` y el UPDATE igual
aparece en el log. Llamar a `save()` sobre una entidad ya administrada no está mal, pero es
redundante: `SimpleJpaRepository.save()` detecta que la entidad no es nueva y ejecuta un
`merge`, que en ese caso no aporta nada. `save()` sí es imprescindible para entidades nuevas
(el `MovimientoStock`, que sin `save()` nunca se persistiría) o para entidades *detached*,
por ejemplo un objeto que llegó deserializado desde JSON y nunca fue cargado en esta
transacción.

**5. ¿Qué ocurriría si el stock se descuenta pero el registro del movimiento falla y no existiera una transacción?**
Sin transacción, cada operación se confirmaría por separado: el `UPDATE productos` quedaría
guardado y el `INSERT movimientos_stock` se perdería. La base quedaría inconsistente —stock
descontado sin evidencia de por qué—, y esa inconsistencia es silenciosa: nadie puede
auditarla después porque justamente el registro que faltó era la evidencia. Peor aún, un
reintento del cliente volvería a descontar. Con `@Transactional` las dos escrituras son
atómicas: o se confirman juntas o se revierten juntas, que es exactamente lo que demuestra
el endpoint `simular-error` — devuelve 500 y el stock sigue intacto.

**6. ¿Qué tipo de excepción provoca rollback por defecto en Spring?**
Las excepciones *unchecked*: `RuntimeException` y sus subclases, y también `Error`. Por eso
`IllegalStateException`, `ReglaNegocioException` y `RecursoNoEncontradoException` —las tres
extienden `RuntimeException`— revierten la transacción al propagarse. Las excepciones
*checked* (las que heredan de `Exception` sin ser `RuntimeException`) **no** provocan rollback
por defecto; si quisiera ese comportamiento tendría que declararlo con
`@Transactional(rollbackFor = MiExcepcion.class)`. Y hay una condición que suele olvidarse:
la excepción debe *propagarse* fuera del método transaccional. Si la capturo dentro y no la
relanzo, Spring no se entera y hace commit igual.

**7. ¿Por qué una transacción no debería mantenerse abierta mientras se realiza una llamada remota lenta?**
Porque una transacción abierta retiene una conexión del pool y, según la operación, bloqueos
de fila en la base de datos. Si dentro de ella llamo a un servicio externo que tarda varios
segundos —o que se cuelga—, esa conexión y esos bloqueos quedan retenidos todo ese tiempo:
con pocas peticiones concurrentes se agota el pool y la aplicación entera deja de responder,
aunque el problema real esté en el tercero. Además, la llamada remota no es transaccional: si
el commit falla después, lo que ya se envió al servicio externo no se puede revertir. Lo
correcto es dejar la transacción lo más corta posible y hacer la llamada remota fuera de ella.
Es el mismo motivo por el que uso `spring.jpa.open-in-view=false`.

**8. ¿Qué problema podría aparecer si devolvemos directamente entidades JPA con relaciones LAZY como respuesta REST?**
Que Jackson intente serializar una relación `LAZY` cuando la sesión de persistencia ya se
cerró y salte `LazyInitializationException` (un 500 sin causa evidente). Mi `MovimientoStock`
tiene `@ManyToOne(fetch = FetchType.LAZY)` hacia `Producto`, así que serializarlo tal cual
fuera de la transacción daría ese error — y por eso no expuse un endpoint que devuelva
movimientos: la evidencia la verifico por SQL. Aparecen también otros problemas: relaciones
bidireccionales que producen recursión infinita, consultas N+1 disparadas por el propio
serializador, y exponer al cliente campos internos que nunca quise publicar, acoplando el
contrato de la API al modelo de base de datos. La solución habitual es devolver DTOs con
exactamente los campos que la API promete, o cargar la relación explícitamente con
`JOIN FETCH` cuando sí se necesita.

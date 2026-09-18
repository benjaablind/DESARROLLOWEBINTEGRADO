# Preguntas de reflexión — Laboratorio 06

Marco Antonio Gonzales del Valle Vargas

**1. ¿Cuál es la diferencia entre JPA e Hibernate?**
JPA (Jakarta Persistence) es la especificación: define las anotaciones (`@Entity`, `@Id`,
`@Column`), el `EntityManager` y el contrato de cómo debe comportarse un ORM en Java, pero
no ejecuta nada. Hibernate es una implementación concreta de esa especificación: es el que
realmente traduce las operaciones sobre entidades a sentencias SQL, administra el contexto
de persistencia y habla con la base por JDBC. Por eso el código usa `jakarta.persistence.*`
(estándar) y, si mañana se cambiara Hibernate por otro proveedor, la entidad no se tocaría.

**2. ¿Qué ventaja ofrece Spring Data JPA frente a implementar acceso JDBC manual?**
Con JDBC manual habría que abrir conexiones, escribir cada `INSERT`/`SELECT`, mapear a mano
cada columna del `ResultSet` al objeto, manejar excepciones `SQLException` y cerrar
recursos. Spring Data JPA elimina ese código repetitivo: basta declarar una interfaz que
extiende `JpaRepository` y ya se dispone de `save()`, `findAll()`, `findById()`,
`existsById()` y `deleteById()`, además de transacciones, traducción de excepciones y
soporte de paginación y de *query methods* derivados del nombre. El desarrollador se
concentra en la lógica de negocio, no en la mecánica del acceso a datos.

**3. ¿Por qué ProductoRepository es una interfaz y aun así puede inyectarse como objeto?**
Porque Spring Data JPA genera la implementación en tiempo de ejecución. Al arrancar, Spring
detecta la interfaz, crea un *proxy* dinámico respaldado por `SimpleJpaRepository` y lo
registra como bean en el contenedor. Cuando `ProductoService` pide un `ProductoRepository`
por constructor, Spring le inyecta ese proxy. El programador declara el contrato; el
framework aporta el objeto que lo cumple.

**4. ¿Qué función cumple @Entity?**
Marca la clase como una entidad administrada por JPA, es decir, le indica al proveedor de
persistencia que esa clase se corresponde con una tabla y que sus instancias pueden
guardarse, consultarse, actualizarse y eliminarse. Sin `@Entity` la clase es un objeto Java
común y Hibernate lanza el error *Not a managed type*. Junto a `@Table(name = "productos")`
se define además a qué tabla se mapea.

**5. ¿Qué ocurre con el identificador cuando se utiliza GenerationType.IDENTITY?**
El identificador lo genera la base de datos con su columna `AUTO_INCREMENT`, no la
aplicación. Al hacer `save()` de un producto con `id = null`, Hibernate ejecuta el `INSERT`
sin el id (se ve como `values (?, ?, ?, ?, default)`), MySQL asigna el siguiente número y
Hibernate recupera ese valor y lo coloca en el objeto. Por eso la respuesta del `POST` ya
trae el `id`. Una consecuencia práctica: con IDENTITY el `INSERT` no se puede diferir ni
agrupar en lote, porque Hibernate necesita el id de inmediato.

**6. ¿Por qué el Controller de la semana 5 necesitó pocos cambios al introducir la base de datos?**
Porque el Controller solo depende de `ProductoService` y de sus firmas (`listar()`,
`buscarPorId()`, `crear()`, `actualizar()`, `actualizarPrecio()`, `eliminar()`), no de cómo
se almacenan los datos. Lo que cambió fue el interior del Service: donde antes había un
`ConcurrentHashMap` con un `AtomicLong`, ahora hay llamadas al `ProductoRepository`. Como
esas firmas se mantuvieron (incluido el `Optional` para "no existe"), la capa web quedó
intacta. Ese es el beneficio concreto de trabajar por capas: se sustituye el mecanismo de
almacenamiento sin reescribir la API.

**7. ¿Qué riesgo existe al usar ddl-auto=update en producción?**
`update` deja que Hibernate modifique el esquema automáticamente comparándolo con las
entidades, y eso en producción es peligroso: aplica cambios sin revisión ni control de
versiones, no se puede revertir, no detecta renombres (un campo renombrado se convierte en
una columna nueva y los datos de la anterior quedan huérfanos), nunca elimina ni corrige
columnas obsoletas y puede bloquear tablas grandes durante el arranque. Además, un error en
una entidad se propaga directamente a la base real. En producción corresponde
`ddl-auto=validate` (o `none`) y una herramienta de migraciones versionadas como Flyway o
Liquibase, donde cada cambio de esquema es un script revisado, aplicado en orden y
reproducible en todos los ambientes.

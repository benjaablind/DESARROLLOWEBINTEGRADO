[PORTADA]

# 2. Índice

[TOC]

[SALTO]

# 3. Resumen Ejecutivo

El presente informe documenta el desarrollo del **Sistema de Gestión Odontológica**, una aplicación web construida como proyecto final del curso Desarrollo Web Integrado. El sistema atiende la operación diaria de una clínica dental: registro de pacientes, agenda de citas, historia clínica, odontograma, tratamientos, pagos, archivos clínicos y reportes de gestión.

La aplicación sigue una arquitectura de tres capas. El back-end es una **API RESTful desarrollada con Java y Spring Boot**, organizada en controladores, servicios, repositorios, entidades de dominio, objetos de transferencia de datos (DTO) y manejo centralizado de excepciones. El front-end es una **aplicación Angular** con componentes independientes, señales, formularios reactivos, rutas protegidas y estilos SCSS, que consume la API mediante `HttpClient`. La comunicación entre ambos se realiza sobre JSON y está habilitada por una configuración CORS explícita.

El desarrollo se ejecuta por etapas, siguiendo el avance del curso. **Este informe corresponde al cierre de la primera etapa (semanas 1 a 4)**, cuyo objetivo fue construir la API RESTful completa con buenas prácticas, inyección de dependencias y desarrollo guiado por pruebas. En esta etapa la persistencia es en memoria de manera deliberada: la incorporación de PostgreSQL con JPA e Hibernate, así como Spring Security y JWT, corresponde a las semanas 6 a 10 del sílabo, y el despliegue en la nube a las semanas 16 a 18.

**Resultados principales obtenidos:**

- **69 endpoints REST** distribuidos en **11 controladores**, que cubren los diez módulos funcionales del sistema.
- **102 clases Java** en el código de producción, organizadas en siete paquetes por responsabilidad.
- **156 pruebas automatizadas** con JUnit 5 y MockMvc, ejecutadas con resultado **0 fallos y 0 errores**.
- **13 pantallas Angular** operativas que consumen la API en su totalidad.
- Un flujo de trabajo colaborativo en GitHub con **seis ramas de funcionalidad** e integración mediante Pull Request revisado.

Palabras clave: API RESTful, Spring Boot, Angular, TDD, arquitectura por capas, gestión odontológica.

[SALTO]

# 4. Introducción

## 4.1. Contexto del proyecto

Una parte importante de los consultorios y clínicas odontológicas de tamaño pequeño y mediano en el Perú sigue administrando su información en cuadernos, hojas de cálculo o programas de escritorio instalados en una sola computadora. Este esquema genera problemas concretos: la historia clínica se extravía o se sobrescribe, la agenda se cruza porque dos personas anotan la misma hora, el estado de las piezas dentales no tiene trazabilidad y el saldo de un tratamiento se calcula a mano.

El Sistema de Gestión Odontológica nace para atender esa realidad. Centraliza la información clínica y administrativa en una aplicación web accesible desde cualquier equipo de la clínica, conserva el historial completo de cada paciente sin sobrescribirlo y calcula automáticamente los indicadores que la administración necesita: citas del día, tratamientos activos, pagos pendientes e ingresos del período.

El proyecto es además un ejercicio académico: su construcción sigue el orden de aprendizaje del curso Desarrollo Web Integrado, de modo que cada unidad del sílabo incorpora una capa nueva sobre una base ya probada.

## 4.2. Justificación de la elección de tecnologías

**Java y Spring Boot para el back-end.** Spring Boot reduce a lo mínimo la configuración necesaria para levantar un servidor web productivo: incorpora un contenedor de servlets embebido, autoconfiguración por convención y un modelo de inyección de dependencias que hace explícitas las relaciones entre componentes. Su ecosistema cubre además las etapas siguientes del proyecto sin cambiar de marco de trabajo: Spring Data JPA para persistencia y Spring Security con JWT para autenticación. Esa continuidad fue determinante en la elección, porque permite añadir capas sin reescribir lo construido.

**Arquitectura RESTful.** Separar el servidor de la interfaz mediante una API REST sobre HTTP y JSON permite que el back-end y el front-end evolucionen por separado, que el mismo servidor atienda en el futuro a una aplicación móvil y que cada endpoint sea verificable de forma independiente con herramientas como Postman o `curl`. REST aporta además un vocabulario uniforme —recursos identificados por URI, verbos HTTP con semántica definida y códigos de estado estandarizados— que ordena el diseño y reduce las decisiones arbitrarias.

**Angular para el front-end.** Angular ofrece en un solo paquete lo que la aplicación necesita: enrutamiento con carga diferida, formularios reactivos con validación declarativa, cliente HTTP con interceptores y un sistema de inyección de dependencias equivalente en concepto al de Spring, lo que reduce la distancia conceptual entre ambos lados del proyecto. El uso de TypeScript aporta tipado estático: las interfaces del front-end reflejan los DTO del back-end y cualquier desalineación se detecta al compilar y no en tiempo de ejecución.

**MySQL/PostgreSQL como motor relacional.** El dominio odontológico es fuertemente relacional —un paciente tiene citas, historias, tratamientos, pagos y archivos— y exige integridad referencial y transacciones. Un motor relacional accedido mediante JPA e Hibernate es la elección natural, y su incorporación está programada para la etapa correspondiente del curso.

## 4.3. Objetivos

### Objetivo general

Desarrollar una aplicación web para la gestión integral de una clínica odontológica, compuesta por una API RESTful implementada con Java y Spring Boot y un front-end desarrollado con Angular, aplicando las prácticas de arquitectura por capas, inyección de dependencias y desarrollo guiado por pruebas estudiadas en el curso.

### Objetivos específicos

1. Diseñar e implementar una API RESTful con Spring Boot que exponga los diez módulos funcionales del sistema mediante recursos, verbos HTTP y códigos de estado coherentes con el estilo REST.
2. Estructurar el back-end en capas de controlador, servicio y repositorio, resolviendo las dependencias entre ellas por inyección a través del constructor.
3. Aplicar desarrollo guiado por pruebas (TDD) con JUnit 5 y MockMvc, de modo que cada regla de negocio quede respaldada por al menos una prueba automatizada.
4. Validar los datos de entrada de forma declarativa mediante DTO anotados con Bean Validation y centralizar el tratamiento de errores en un único manejador global de excepciones.
5. Desarrollar el front-end con Angular, con componentes independientes, formularios reactivos, rutas protegidas y estilos SCSS, que consuma la totalidad de la API.
6. Integrar back-end y front-end sobre JSON con una política CORS explícita, y verificar el funcionamiento conjunto de los diez módulos.
7. Organizar el trabajo de los seis integrantes en GitHub mediante ramas de funcionalidad e integración por Pull Request revisado.
8. Preparar la evolución del sistema hacia persistencia relacional, seguridad con JWT y despliegue en la nube, dejando el código estructurado para incorporarlas sin reescritura.

[SALTO]

# 5. Marco Teórico

## 5.1. Antecedentes Nacionales e Internacionales

**Antecedente nacional 1.** Mosquera Pérez y Mauro Silva (2024), en la Universidad Nacional de la Amazonía Peruana, desarrollaron un *Sistema web de gestión de citas e historias clínicas para el centro odontológico especializado Disan Cuñumbuqui – San Martín*. El trabajo parte de un diagnóstico frecuente en el sector: el registro manual de citas e historias clínicas produce duplicidad de horarios, pérdida de información y demora en la atención. Los autores implementan una solución web que centraliza ambos procesos y reportan mejoras en el tiempo de registro y en la disponibilidad de la información clínica. La relación con el presente proyecto es directa: los módulos de citas e historia clínica del Sistema de Gestión Odontológica atienden el mismo problema, y el criterio de no sobrescribir la información histórica que aquí se adopta responde a la misma preocupación.

**Antecedente internacional 1.** Díaz Marcos (2019), en la Universidad Politécnica de Madrid, presentó el trabajo *Desarrollo de una aplicación web con Spring Boot y Angular para la gestión de un catálogo de productos*. El estudio documenta exactamente la combinación tecnológica de este proyecto: un back-end Spring Boot que expone una API REST y un cliente Angular que la consume, desplegados sobre una plataforma en la nube. Aporta al presente informe la validación de la separación en dos aplicaciones independientes comunicadas por HTTP y JSON, y el tratamiento del despliegue como una etapa con requisitos propios.

**Antecedente internacional 2.** Telenchana Chimbo (2022), en la Universidad Técnica de Ambato (Ecuador), desarrolló una *Aplicación web usando el framework Angular para el control de historias clínicas de los pacientes del consultorio médico Fisio&Trauma*. El trabajo detalla el diseño de las pantallas de registro y consulta de la historia clínica y la gestión de horarios de atención con Angular, y concluye que el uso de un framework de componentes reduce el tiempo de desarrollo y mejora la consistencia de la interfaz. Su aporte a este proyecto está en el diseño del front-end: la organización por pantallas de módulo y el uso de formularios con validación declarativa siguen la misma línea.

Los tres antecedentes coinciden en un punto que este proyecto retoma: el valor del sistema no está en digitalizar formularios, sino en garantizar la trazabilidad de la información clínica y la coherencia de la agenda.

## 5.2. Desarrollo de APIs RESTful con Spring Boot

**Conceptos de APIs REST.** REST (Representational State Transfer) es un estilo arquitectónico definido por Fielding (2000) para sistemas distribuidos sobre HTTP. Sus restricciones principales son la separación cliente-servidor, la ausencia de estado de sesión en el servidor —cada solicitud lleva toda la información necesaria—, la identificación de recursos mediante URI y el uso de una interfaz uniforme. En la práctica, una API REST expone recursos sustantivos (`/api/pacientes`, `/api/citas`) sobre los que se opera con los verbos del protocolo, y devuelve representaciones en JSON acompañadas de un código de estado que comunica el resultado.

**Verbos HTTP y códigos de estado.** `GET` consulta sin efectos secundarios; `POST` crea un recurso nuevo y responde `201 Created` con la cabecera `Location`; `PUT` reemplaza por completo un recurso existente; `PATCH` modifica solo una parte; `DELETE` elimina y responde `204 No Content`. Los errores se comunican con `400 Bad Request` cuando los datos de entrada o las reglas de negocio no se cumplen, y con `404 Not Found` cuando el recurso solicitado no existe.

**Spring Boot: características y ventajas.** Spring Boot es una capa de convenciones sobre el Spring Framework. Sus aportes principales son la autoconfiguración —el contenedor deduce la configuración a partir de las dependencias presentes en el `classpath`—, los *starters*, que agrupan dependencias coherentes entre sí, y el servidor embebido, que permite empaquetar la aplicación como un ejecutable autónomo. Walls (2022) resume su propuesta como la eliminación del trabajo repetitivo de configuración para dejar al desarrollador ocupado únicamente de la lógica del negocio.

**Endpoints y controladores.** La anotación `@RestController` marca una clase cuyos métodos devuelven directamente el cuerpo de la respuesta serializado a JSON. `@RequestMapping` fija la ruta base del recurso y las anotaciones `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping` y `@DeleteMapping` asocian cada método a un verbo. Los datos de entrada llegan por tres vías: `@PathVariable` para los segmentos variables de la ruta, `@RequestParam` para los parámetros de consulta y `@RequestBody` para el cuerpo JSON. El tipo `ResponseEntity<T>` permite controlar de forma explícita el código de estado y las cabeceras de la respuesta.

**Inyección de dependencias.** La inyección de dependencias es la aplicación del principio de inversión de control: un componente no construye sus colaboradores, sino que los recibe ya construidos del contenedor. Spring administra beans anotados con `@RestController`, `@Service`, `@Repository` o `@Configuration` y los entrega donde se declaran. La forma recomendada es la **inyección por constructor**, porque permite declarar los campos como `final`, deja explícitas las dependencias de la clase, evita objetos en estado incompleto y facilita las pruebas: en un caso de prueba basta con construir la clase pasando un doble en lugar de la dependencia real.

**Desarrollo guiado por pruebas (TDD).** Beck (2003) define el TDD como un ciclo de tres pasos: **Red**, escribir una prueba que falla porque la funcionalidad todavía no existe; **Green**, escribir el código mínimo que la hace pasar; **Refactor**, mejorar el diseño con la red de seguridad de la prueba ya verde. Sus efectos prácticos son un diseño más desacoplado —el código nace preparado para ser invocado desde una prueba— y una suite de regresión que crece junto con el sistema. En Spring Boot el ciclo se apoya en JUnit 5 como motor de pruebas, Mockito para sustituir colaboradores por dobles y MockMvc para ejercitar los controladores enviando solicitudes HTTP simuladas sin levantar un servidor real.

## 5.3. Back-end con Bases de Datos

**Manejo de datos con JPA e Hibernate.** JPA (Jakarta Persistence API) es la especificación estándar de mapeo objeto-relacional en Java, e Hibernate su implementación más difundida. Las entidades se anotan con `@Entity`, su clave primaria con `@Id` y `@GeneratedValue`, y las asociaciones con `@OneToMany`, `@ManyToOne` o `@ManyToMany`. El proveedor traduce las operaciones sobre objetos a sentencias SQL y administra el ciclo de vida de las instancias dentro de un contexto de persistencia.

**Operaciones CRUD.** Spring Data JPA genera la implementación de los repositorios a partir de interfaces: al extender `JpaRepository<T, ID>` se obtienen `save`, `findById`, `findAll`, `deleteById` y `count` sin escribir código, y se pueden declarar consultas derivadas del nombre del método, como `findByDni(String dni)`.

**Consultas JPQL.** JPQL consulta sobre el modelo de entidades y no sobre las tablas. Se declara con `@Query` y admite parámetros con nombre, por ejemplo `SELECT p FROM Paciente p WHERE p.estado = :estado`. Resulta útil cuando la consulta excede lo que el nombre de un método puede expresar con claridad.

**Transacciones en Spring Boot.** La anotación `@Transactional` delimita una unidad de trabajo: si el método termina con normalidad la transacción se confirma y, si lanza una excepción no controlada, se revierte por completo. En el dominio de este proyecto la operación de registrar un pago y actualizar el saldo del tratamiento es un caso típico: ambas acciones deben ocurrir juntas o ninguna.

**Seguridad con Spring Security.** Spring Security intercepta las solicitudes mediante una cadena de filtros y resuelve dos preguntas distintas: la autenticación —quién es el usuario— y la autorización —qué le está permitido—. Las contraseñas se almacenan cifradas con un algoritmo de hash con sal, como BCrypt, y las reglas de acceso se declaran por ruta o por método con anotaciones como `@PreAuthorize`.

**Autenticación con JWT.** Un JSON Web Token es una credencial firmada compuesta por cabecera, cuerpo de afirmaciones y firma. Tras un inicio de sesión válido el servidor emite el token; el cliente lo envía en la cabecera `Authorization: Bearer <token>` en cada solicitud, y el servidor lo verifica con su clave sin consultar ninguna sesión almacenada. Este esquema es coherente con la restricción REST de ausencia de estado en el servidor.

## 5.4. Integración de Front-end con Angular

**Fundamentos: componentes, módulos y servicios.** La unidad de construcción de Angular es el componente: una clase TypeScript con su plantilla HTML y su hoja de estilos. Desde la versión 17 los componentes independientes (*standalone*) declaran sus propias dependencias y hacen innecesario el `NgModule` para la mayoría de los casos. Los servicios concentran la lógica reutilizable —típicamente el acceso HTTP— y se obtienen por inyección de dependencias con la función `inject()`.

**Estilos con SASS/SCSS.** SCSS extiende CSS con variables, anidamiento, mixins y funciones. Angular admite estilos por componente con encapsulación automática, de modo que las reglas de un componente no se filtran a otro, y una hoja global para tokens de color, tipografía y espaciado compartidos.

**Rutas y comunicación entre componentes.** El `Router` asocia rutas a componentes y admite carga diferida con `loadComponent`, de modo que el código de cada pantalla se descarga solo cuando se visita. Los guardias de ruta (`CanActivateFn`) deciden si una navegación puede completarse. La comunicación entre componentes se resuelve con entradas y salidas para relaciones padre-hijo, y con servicios compartidos o señales para el estado que trasciende la jerarquía.

**Formularios y validación.** Los formularios reactivos construyen el modelo en la clase con `FormBuilder`, `FormGroup` y `FormControl`, y aplican validadores declarativos (`Validators.required`, `Validators.email`, `Validators.minLength`). El estado de cada control —`valid`, `touched`, `dirty`— permite mostrar los mensajes de error en el momento oportuno. Esta validación es de conveniencia para el usuario y no reemplaza a la del servidor, que es la única que garantiza la integridad de los datos.

**Consumo de APIs REST.** `HttpClient` devuelve `Observable`, lo que permite componer la respuesta con los operadores de RxJS y capturar los fallos con `catchError`. La práctica recomendada es concentrar el acceso HTTP en servicios y mantener los componentes ocupados solo de la presentación.

**Autenticación y autorización en Angular.** Del lado del cliente, la autenticación consiste en conservar la credencial emitida por el servidor, adjuntarla a cada solicitud mediante un interceptor y proteger las rutas con guardias. Es importante subrayar que estos controles mejoran la experiencia de uso pero no constituyen seguridad real: la autorización efectiva siempre se resuelve en el servidor.

## 5.5. Despliegue de Aplicaciones Web

**Servidores web y servicios en la nube.** Una aplicación Spring Boot se empaqueta como un archivo JAR ejecutable con servidor embebido, mientras que Angular se compila a archivos estáticos que un servidor web como Nginx o Apache entrega al navegador. Es habitual situar un proxy inverso delante de ambos: sirve los archivos estáticos, redirige las rutas `/api` al back-end y termina la conexión TLS. Las plataformas en la nube —AWS, Google Cloud, Render, Railway— añaden el aprovisionamiento del servidor, la base de datos administrada y los certificados.

**Contenedores.** Empaquetar cada componente en una imagen de contenedor elimina las diferencias entre el entorno de desarrollo y el de producción, y permite describir el conjunto —back-end, front-end y base de datos— en un solo archivo de composición.

**Integración y despliegue continuos (CI/CD).** Una tubería de CI/CD ejecuta automáticamente, ante cada cambio publicado, la compilación y la suite de pruebas, y despliega solo si todo resulta correcto. Con GitHub Actions esto se describe en un archivo de flujo de trabajo dentro de `.github/workflows`. El valor de esta práctica depende directamente de la calidad de las pruebas automatizadas: una tubería sobre una suite pobre da una confianza que no corresponde.

[SALTO]

# 6. Desarrollo del Proyecto

## 6.1. Implementación de la API RESTful con Spring Boot

### 6.1.1. Configuración del proyecto

El proyecto se generó con Spring Initializr sobre Maven, con Java 25 y Spring Boot 3.5.6. Las dependencias declaradas en `pom.xml` corresponden estrictamente al alcance de esta etapa:

| Dependencia | Propósito |
|---|---|
| `spring-boot-starter-web` | Servidor embebido, Spring MVC y serialización JSON con Jackson |
| `spring-boot-starter-validation` | Bean Validation: `@Valid` y anotaciones de restricción |
| `spring-boot-devtools` | Recarga automática durante el desarrollo |
| `spring-boot-starter-test` | JUnit 5, MockMvc, Mockito, AssertJ y Hamcrest |

El archivo `pom.xml` deja constancia por escrito, en un comentario, de las dependencias que **todavía no** se incorporan —`spring-boot-starter-data-jpa`, el conector de la base de datos y `spring-boot-starter-security`— y de la semana en que corresponde añadirlas. La configuración de la aplicación se concentra en `application.properties`: puerto del servidor, detalle de los mensajes de error, límites de tamaño para la subida de archivos, carpeta de almacenamiento y orígenes permitidos para CORS.

### 6.1.2. Arquitectura por capas

El código de producción está organizado en siete paquetes, cada uno con una responsabilidad única:

| Paquete | Responsabilidad |
|---|---|
| `controller` | Traduce HTTP: recibe la solicitud, delega y construye la respuesta |
| `service` | Reglas de negocio y validaciones de dominio |
| `repository` | Acceso y almacenamiento de las entidades |
| `model` | Entidades del dominio y enumeraciones de estado |
| `dto` | Objetos de entrada y salida, con las restricciones de validación |
| `exception` | Excepciones propias y manejador global |
| `config` | CORS y carga de datos de demostración |

La regla de dependencia es unidireccional: el controlador conoce al servicio, el servicio conoce al repositorio, y nunca al revés. Ninguna entidad del dominio se expone directamente al exterior; la conversión hacia los DTO de respuesta se realiza mediante métodos estáticos de fábrica, por ejemplo `PacienteResponse.desde(paciente)`.

### 6.1.3. Creación de endpoints y controladores

Se implementaron **11 controladores** que suman **69 endpoints**:

| Verbo | Cantidad |
|---|---|
| GET | 33 |
| POST | 12 |
| PUT | 5 |
| PATCH | 12 |
| DELETE | 7 |
| **Total** | **69** |

El diseño de las rutas sigue el estilo REST: el recurso en plural (`/api/pacientes`), el identificador como segmento de ruta (`/api/pacientes/{id}`), los filtros como parámetros de consulta (`/api/pacientes?texto=&estado=`) y las acciones que modifican solo una parte del recurso como subrecursos con `PATCH` (`/api/citas/{id}/confirmar`). La creación responde `201 Created` con la cabecera `Location` apuntando al recurso recién creado, y la eliminación responde `204 No Content`.

Un ejemplo del criterio de diseño: el módulo de historia clínica **no expone `PUT` ni `DELETE` de forma deliberada**. La información histórica no se sobrescribe ni se borra; una consulta registrada por error se anula con `PATCH /api/historias/{id}/anular` indicando el motivo, y permanece visible en el historial. El mismo criterio rige en el odontograma: cada cambio de estado de una pieza crea un registro nuevo y el estado actual es simplemente el último registro, de modo que el historial de cada pieza queda completo.

### 6.1.4. Implementación de la inyección de dependencias

Todas las dependencias se resuelven **por constructor**, sin `@Autowired` sobre campos. Cada controlador declara su servicio como campo `final` y lo recibe en el constructor; cada servicio hace lo mismo con su repositorio. El efecto es doble: la clase no puede construirse en un estado incompleto, y las dependencias de cada componente se leen de un vistazo en su firma. El fragmento correspondiente se incluye en el Anexo 12.1.

### 6.1.5. Validación y manejo de errores

Las validaciones de formato se declaran en los DTO de entrada con anotaciones de Bean Validation —`@NotBlank`, `@NotNull`, `@Pattern`, `@Email`, `@Past`, `@Positive`— y se activan con `@Valid` en el parámetro del controlador. Las validaciones de negocio, que requieren consultar el estado del sistema —documento de identidad repetido, cruce de horarios en la agenda, monto de pago superior al saldo pendiente—, viven en la capa de servicio y se comunican lanzando `ReglaNegocioException` o `RecursoNoEncontradoException`.

Ningún controlador contiene bloques `try/catch`. La clase `ApiExceptionHandler`, anotada con `@RestControllerAdvice`, concentra el tratamiento de errores de toda la API y devuelve siempre la misma estructura JSON:

```json
{
  "estado": 404,
  "mensaje": "No existe paciente con id: 99",
  "ruta": "/api/pacientes/99",
  "fechaHora": "2026-09-04T16:44:26.47"
}
```

Se atienden ocho tipos de fallo distintos: recurso inexistente, regla de negocio incumplida, error de validación de DTO, violación de restricción sobre parámetros, JSON mal formado, tipo de parámetro incorrecto, parámetro obligatorio ausente y archivo que supera el tamaño permitido.

### 6.1.6. Desarrollo guiado por pruebas (TDD)

El ciclo Red–Green–Refactor se aplicó módulo por módulo. Para cada regla de negocio se escribió primero la prueba que la describe, se verificó que fallara y recién entonces se implementó el código que la satisface. El resultado es una suite de **156 pruebas** repartidas en 10 clases:

| Clase de prueba | Pruebas |
|---|---|
| `UsuarioControllerTest` | 22 |
| `PacienteControllerTest` | 21 |
| `TratamientoControllerTest` | 21 |
| `CitaControllerTest` | 20 |
| `ArchivoControllerTest` | 14 |
| `HistoriaClinicaControllerTest` | 14 |
| `OdontogramaControllerTest` | 14 |
| `PagoControllerTest` | 13 |
| `ReporteControllerTest` | 10 |
| `DashboardControllerTest` | 7 |
| **Total** | **156** |

Cada clase se anota con `@SpringBootTest` y `@AutoConfigureMockMvc`, e inyecta `MockMvc` para enviar solicitudes HTTP simuladas contra los controladores reales, con todo el contexto de Spring activo: validaciones, conversión JSON y manejador de excepciones incluidos. Un método `@BeforeEach` limpia el repositorio y carga un conjunto conocido de datos, de modo que cada prueba parte de un estado determinado y el orden de ejecución es irrelevante.

Las pruebas no se limitan al camino feliz. Junto a cada caso correcto se verifica el comportamiento ante el error: documento repetido, cita solapada, pago mayor al saldo, número de pieza dental fuera de la notación FDI, fecha inicial posterior a la final. La verificación se realiza sobre el código de estado, las cabeceras y el contenido del JSON mediante `jsonPath`.

### 6.1.7. Pruebas de la API con Postman

En paralelo a las pruebas automatizadas, cada integrante verificó sus endpoints con Postman y con `curl`. Se documentaron los ejemplos de invocación de todos los módulos en `backend/DOCUMENTACION_API.md`, incluyendo el cuerpo de cada solicitud y la respuesta esperada. Las evidencias de estas ejecuciones se presentan en el Anexo 12.3.

## 6.2. Back-end con Bases de Datos

Esta sección corresponde a las **semanas 6 a 10** del curso. A la fecha de este informe el contenido no ha sido dictado, por lo que se documenta el modelado ya realizado y la estrategia de migración preparada, y se identifica con claridad lo que queda pendiente de implementación.

### 6.2.1. Modelado de la base de datos

El modelo de dominio ya está definido y expresado en las clases del paquete `model`. Estas clases son el borrador directo de las tablas de la futura base de datos:

| Entidad | Atributos principales | Relaciones |
|---|---|---|
| `Usuario` | usuario, contraseña, nombres, correo, rol, activo | Registra citas, historias y archivos |
| `Paciente` | DNI, nombres, apellidos, fecha de nacimiento, sexo, contacto, estado, antecedentes | 1 a N con citas, historias, tratamientos, pagos y archivos |
| `Cita` | fecha, hora, motivo, estado, paciente, odontólogo | N a 1 con paciente y usuario |
| `HistoriaClinica` | fecha, motivo, anamnesis, examen, diagnóstico, procedimiento, medicamentos | N a 1 con paciente y usuario |
| `RegistroPieza` | número de pieza (FDI), estado, superficie, diagnóstico, fecha | N a 1 con paciente |
| `Tratamiento` | descripción, precio, estado, fechas, sesiones | 1 a N con sesiones y pagos |
| `Pago` | monto, método, fecha | N a 1 con tratamiento y paciente |
| `Archivo` | nombre, tipo, extensión, tamaño, ubicación, fecha | N a 1 con paciente e historia |

Las enumeraciones `EstadoCita`, `EstadoPaciente`, `EstadoPieza`, `EstadoTratamiento`, `MetodoPago`, `TipoArchivo` y `Rol` fijan los valores admitidos de cada campo de estado y pasarán a columnas con restricción de dominio.

### 6.2.2. Persistencia actual y estrategia de migración

En esta etapa la persistencia es **en memoria**, de manera deliberada y documentada. Todos los repositorios extienden la clase abstracta `RepositorioEnMemoria<T>`, que almacena las entidades en un `ConcurrentHashMap` y genera los identificadores con un `AtomicLong`. La elección de estructuras concurrentes no es casual: los beans de Spring son singleton y varias solicitudes HTTP pueden operar sobre el mismo repositorio simultáneamente.

La estrategia de migración está preparada de antemano: los servicios solo invocan los métodos del repositorio (`guardar`, `buscarPorId`, `listar`, `listarSi`, `eliminar`), nunca la estructura de datos subyacente. Cuando se incorpore la unidad de bases de datos, cada repositorio pasará a extender `JpaRepository`, las entidades recibirán las anotaciones `@Entity`, `@Id` y las de asociación, y **los servicios y controladores no cambiarán**. Esta es la razón práctica por la que la arquitectura por capas se respetó con rigor desde la primera semana.

### 6.2.3. Pendiente por etapa del curso

Quedan pendientes, para las semanas 6 a 10: la configuración del origen de datos y de Hibernate, la implementación de las operaciones CRUD sobre `JpaRepository`, las consultas personalizadas con JPQL, la gestión de transacciones con `@Transactional` —con prioridad en el registro de pagos y la actualización del saldo— y la configuración de Spring Security con roles, permisos y autenticación mediante JWT. El paquete `security` ya existe en el árbol del proyecto, reservado para esa etapa.

## 6.3. Desarrollo del Front-end con Angular

Esta sección corresponde a las **semanas 11 a 15**. El equipo la adelantó respecto del cronograma del curso, por lo que se documenta lo implementado dejando constancia de que su exposición formal corresponde a una etapa posterior.

### 6.3.1. Configuración del proyecto

El front-end se generó con Angular CLI sobre Angular 21 y TypeScript 5.9, con SCSS como preprocesador de estilos. Los archivos `src/environments/environment.ts` y `environment.development.ts` concentran la URL base de la API (`http://localhost:8080/api`), de modo que el cambio de servidor al desplegar no toca ninguna otra línea de código.

### 6.3.2. Componentes y estilos

La aplicación está compuesta por **13 componentes de página** independientes: inicio de sesión, panel de indicadores, listado de pacientes, ficha del paciente, citas, historias clínicas, odontograma, tratamientos, pagos, archivos, reportes, usuarios y página no encontrada. Un componente de disposición (`layout`) monta la barra lateral, la barra superior y el área de notificaciones comunes a todas las pantallas. Cada componente declara `ChangeDetectionStrategy.OnPush` y expone su estado con señales (`signal`), lo que reduce el trabajo de detección de cambios. Los estilos se escriben en SCSS por componente, sobre una hoja global de variables de color, tipografía y espaciado.

### 6.3.3. Rutas y comunicación entre componentes

El enrutamiento está definido en `app.routes.ts` con **carga diferida** de todas las pantallas mediante `loadComponent`, de forma que el navegador descarga únicamente el código de la vista solicitada. La estructura tiene una ruta padre protegida por `authGuard` que monta el `layout` y cuelga de ella todas las pantallas internas; el inicio de sesión es la única ruta fuera de ese contenedor. El guardia redirige a `/login` conservando la ruta solicitada en el parámetro `retorno`, para devolver al usuario a donde quería ir una vez autenticado. La comunicación entre componentes se resuelve a través de los servicios del núcleo y del servicio de notificaciones, compartido por toda la aplicación.

### 6.3.4. Formularios y validación

Las pantallas de registro y edición usan formularios reactivos construidos con `FormBuilder`, con validadores declarativos y mensajes de error por control. La validación del cliente replica las restricciones del back-end —ocho dígitos en el documento, formato de correo, campos obligatorios— con el objetivo de dar retroalimentación inmediata; la validación definitiva sigue siendo la del servidor.

### 6.3.5. Consumo de la API REST

Todo el acceso HTTP pasa por un único servicio, `ApiService`, que envuelve a `HttpClient`. Este servicio construye la URL a partir del entorno, descarta los parámetros de consulta vacíos, ofrece métodos para JSON, `FormData` y descarga de binarios como `Blob`, y —lo más relevante— **normaliza cualquier fallo a una única estructura `ErrorApi`**: si el back-end respondió con su JSON de error se conserva su mensaje; si no hubo respuesta se informa que el servidor no está disponible. Los doce servicios de módulo se apoyan en él, de modo que ningún componente manipula códigos de estado HTTP: solo lee `.mensaje`.

### 6.3.6. Autenticación en el front-end

`AuthService` conserva el usuario devuelto por `POST /api/auth/login` en el almacenamiento del navegador y `authGuard` protege las rutas. Debe subrayarse que **esto no constituye seguridad real**: sin JWT ni Spring Security los endpoints siguen siendo accesibles directamente. La protección efectiva se implementará en la etapa correspondiente, y la estructura actual —un único punto de autenticación y un único cliente HTTP— está preparada para incorporar el interceptor que adjunte el token sin modificar los componentes.

## 6.4. Integración Back-end y Front-end

La comunicación entre ambas aplicaciones se realiza sobre HTTP con cuerpos JSON. Los puntos que hicieron posible la integración fueron cuatro:

1. **Contrato de datos compartido.** Las interfaces TypeScript de `core/models` reflejan campo por campo los DTO del back-end. Cualquier divergencia en un nombre de campo se detecta al compilar el front-end.
2. **Configuración CORS explícita.** `CorsConfig` autoriza el origen `http://localhost:4200` sobre las rutas `/api/**`, habilita los verbos utilizados y **expone la cabecera `Location`**, necesaria para que el cliente conozca la URI del recurso recién creado. El origen permitido se lee de `application.properties`, no está incrustado en el código.
3. **Estructura de error única.** Como todos los errores de la API comparten el mismo formato, el front-end los traduce en un solo lugar y muestra el mensaje del servidor tal como fue redactado.
4. **Datos de demostración.** La clase `DatosIniciales` carga al arrancar un conjunto coherente de usuarios, pacientes, citas, historias, registros de odontograma, tratamientos y pagos, lo que permite verificar la integración de inmediato y sin preparación manual.

Las pruebas de integración de esta etapa se realizaron en dos niveles: automatizadas con MockMvc sobre el contexto completo de Spring, y manuales recorriendo cada pantalla del front-end contra el back-end en ejecución, verificando el resultado en el panel de red del navegador.

## 6.5. Despliegue de la Aplicación

Esta sección corresponde a las **semanas 16 a 18** del curso y **está pendiente de ejecución**. Se documenta el plan definido, sin presentarlo como resultado alcanzado.

**Plan de despliegue previsto:**

- **Empaquetado.** El back-end se empaquetará como JAR ejecutable con `mvn clean package`; el front-end se compilará con `ng build --configuration production`, generando los archivos estáticos.
- **Servidor web.** Nginx actuará como proxy inverso: entregará los archivos estáticos de Angular, redirigirá las solicitudes `/api` al proceso de Spring Boot y terminará la conexión HTTPS.
- **Base de datos.** Una instancia administrada del motor relacional, con credenciales suministradas por variables de entorno y nunca versionadas en el repositorio.
- **Servicio en la nube.** Se evaluarán Render, Railway y AWS Elastic Beanstalk, priorizando el nivel gratuito disponible para estudiantes.
- **CI/CD.** Un flujo de trabajo de GitHub Actions ejecutará `mvn test` y la compilación del front-end ante cada Pull Request, y desplegará automáticamente al integrar en `main`. La suite de 156 pruebas ya existente es la condición que hace útil esta automatización.
- **Configuración por entorno.** La URL de la API en `environment.ts` y los orígenes CORS en `application.properties` son los dos únicos puntos que deben ajustarse al desplegar; ambos ya están externalizados.

[SALTO]

# 7. Resultados

## 7.1. Funcionalidades Implementadas

### Back-end (API RESTful)

| Módulo | Responsable | Funcionalidades | Endpoints |
|---|---|---|---|
| Autenticación | Integrante 1 | Inicio y cierre de sesión, consulta de perfil | 3 |
| Usuarios y roles | Integrante 1 | CRUD, cambio de contraseña, activación y baja lógica, permisos por rol | 9 |
| Pacientes | Integrante 2 | CRUD, búsqueda por texto y documento, antecedentes, cambio de estado | 8 |
| Citas y agenda | Integrante 3 | CRUD, agenda del día, disponibilidad, confirmación, asistencia, cancelación, reprogramación | 12 |
| Dashboard | Integrante 3 | Indicadores generales, próximas citas, alertas, resumen de actividad | 1 |
| Historia clínica | Integrante 4 | Registro de consultas, historial por paciente, anulación con motivo | 4 |
| Odontograma | Integrante 4 | Estado de piezas, historial por pieza, resumen por estado | 4 |
| Tratamientos | Integrante 5 | CRUD, gestión de sesiones, cambio de estado, cálculo de saldo | 9 |
| Pagos | Integrante 5 | Registro de pagos totales y parciales, historial, estado de cuenta | 6 |
| Archivos clínicos | Integrante 6 | Subida multipart, descarga, metadatos, filtros por paciente y tipo | 7 |
| Reportes | Integrante 6 | Pacientes, citas por período, tratamientos, ingresos, pagos pendientes, historial | 6 |

**Reglas de negocio implementadas y verificadas:**

- Un odontólogo no puede tener dos citas que se solapen en el mismo horario.
- Desde los estados `CANCELADA` o `ATENDIDA` una cita no puede volver a otro estado.
- El documento de identidad de un paciente debe tener ocho dígitos y no puede repetirse.
- La edad del paciente se calcula automáticamente a partir de la fecha de nacimiento.
- Una consulta de historia clínica no se edita ni se borra: se anula indicando el motivo y permanece en el historial.
- Cada cambio en una pieza dental crea un registro nuevo; el estado actual es el último registro.
- Los números de pieza deben pertenecer a la notación FDI (11–48 en dentición permanente, 51–85 en decidua).
- Un tratamiento completado o cancelado no admite modificaciones ni sesiones nuevas.
- No se elimina un tratamiento que ya tiene pagos registrados.
- El monto de un pago no puede superar el saldo pendiente y el saldo nunca queda negativo.
- Los archivos aceptan solo las extensiones permitidas, con un máximo de diez megabytes, y reciben un nombre único en disco.
- La ruta física de un archivo nunca aparece en las respuestas de la API.

### Front-end (Angular)

Trece pantallas operativas: inicio de sesión, panel de indicadores, listado y ficha de pacientes, citas con agenda del día, historias clínicas, odontograma, tratamientos con sesiones, pagos con estado de cuenta, archivos clínicos, reportes, administración de usuarios y página de ruta no encontrada. Todas consumen la API real; ninguna trabaja con datos simulados.

## 7.2. Pruebas y Validaciones

### Resultado de la ejecución de la suite automatizada

Ejecución de `mvn test` sobre el proyecto del back-end:

```
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0 -- ArchivoControllerTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0 -- CitaControllerTest
[INFO] Tests run:  7, Failures: 0, Errors: 0, Skipped: 0 -- DashboardControllerTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0 -- HistoriaClinicaControllerTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0 -- OdontogramaControllerTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0 -- PacienteControllerTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- PagoControllerTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 -- ReporteControllerTest
[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0 -- TratamientoControllerTest
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0 -- UsuarioControllerTest
[INFO]
[INFO] Results:
[INFO] Tests run: 156, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 20.944 s
```

**Resumen:** 156 pruebas ejecutadas, 156 exitosas, 0 fallos, 0 errores, 0 omitidas. Tiempo total de la construcción con las pruebas incluidas: 20,9 segundos.

### Cobertura funcional de las pruebas

| Tipo de verificación | Ejemplos |
|---|---|
| Camino correcto | Listado, consulta por identificador, creación con `201` y `Location`, actualización, eliminación con `204` |
| Validación de entrada | Documento con formato inválido, campos obligatorios ausentes, correo mal formado, JSON mal formado |
| Reglas de negocio | Documento repetido, cruce de horarios, pago superior al saldo, tratamiento cerrado, pieza fuera de la notación FDI |
| Recurso inexistente | `404` con mensaje descriptivo en todos los módulos |
| Filtros y consultas | Combinación de parámetros de consulta, rangos de fechas, agenda de un día |

### Pruebas manuales con Postman y curl

Cada integrante verificó los endpoints de su módulo con Postman antes de solicitar la integración. Las evidencias de la ejecución sobre el servidor en funcionamiento, con las respuestas reales de la API, se presentan en el Anexo 12.3.

## 7.3. Despliegue

El despliegue en un servidor web o en un servicio en la nube corresponde a las semanas 16 a 18 del curso y **no ha sido ejecutado a la fecha de este informe**. Lo verificado hasta ahora es la ejecución local completa del sistema: el back-end en `http://localhost:8080/api`, el front-end en `http://localhost:4200`, ambos comunicándose correctamente con los datos de demostración cargados al arranque. El plan de despliegue previsto se documenta en la sección 6.5.

[SALTO]

# 8. Discusión

## 8.1. Análisis de los resultados obtenidos

El objetivo de la primera etapa era construir una API RESTful completa aplicando arquitectura por capas, inyección de dependencias y TDD. Los resultados indican que se alcanzó: 69 endpoints cubren la totalidad del dominio funcional definido y las 156 pruebas automatizadas se ejecutan sin fallos.

El resultado más significativo no es, sin embargo, el número de endpoints, sino el efecto de las decisiones de diseño. Concentrar el manejo de errores en `@RestControllerAdvice` eliminó por completo los bloques `try/catch` de los once controladores y garantizó que un cliente reciba siempre la misma estructura de error, independientemente del módulo que falle. Ese único acuerdo permitió, más adelante, que el front-end tradujera cualquier fallo de la API en un solo lugar.

De forma equivalente, mantener las reglas de negocio fuera de los controladores hizo que los controladores quedaran reducidos a lo que deben ser: una traducción entre HTTP y el dominio. Un controlador que solo delega es corto, legible y fácil de probar.

## 8.2. Dificultades encontradas y soluciones implementadas

**Coordinación de seis personas sobre un mismo proyecto.** El riesgo principal era el conflicto permanente en archivos compartidos. La solución fue doble: asignar a cada integrante un módulo con sus propias clases —de manera que los archivos que cada uno modifica sean, en su mayoría, exclusivamente suyos— y acordar de antemano los nombres de entidades, DTO y rutas. Los cambios se integraron mediante Pull Request con revisión de otro integrante, y nadie modificó `main` directamente.

**Persistencia sin base de datos.** El curso exige respetar el orden de las unidades, pero el sistema debía funcionar desde la primera etapa. Se resolvió con la clase abstracta `RepositorioEnMemoria<T>`, que expone exactamente el mismo tipo de operaciones que ofrecerá `JpaRepository`. El costo de la decisión es conocido y está asumido: los datos se pierden al reiniciar el servidor. El beneficio es que la migración posterior no obligará a tocar los servicios.

**Pruebas que se interferían entre sí.** Al compartir un repositorio singleton, los datos que dejaba una prueba afectaban a la siguiente y los resultados variaban según el orden de ejecución. Se resolvió con un método `limpiar()` en el repositorio base, invocado desde un `@BeforeEach` que reconstruye el conjunto de datos conocido antes de cada prueba.

**Concurrencia en el almacenamiento en memoria.** Un `HashMap` corriente no es seguro cuando varias solicitudes HTTP operan a la vez sobre el mismo bean singleton. Se sustituyó por `ConcurrentHashMap` y la generación de identificadores por un `AtomicLong`.

**Bloqueo por CORS al integrar.** Las primeras llamadas del front-end fueron rechazadas por el navegador y, una vez habilitado el origen, el cliente seguía sin poder leer la URI del recurso creado. La causa era que la cabecera `Location` no estaba expuesta. La solución fue añadir `exposedHeaders("Location")` a la configuración CORS y leer el origen permitido desde `application.properties`.

**Diseño del odontograma.** El primer diseño actualizaba el estado de la pieza dental sobre el mismo registro, lo que hacía imposible reconstruir su historia. Se rediseñó como una bitácora de solo inserción: cada cambio crea un registro y el estado vigente es el último. El mismo criterio se aplicó a la historia clínica, que se anula pero no se borra.

## 8.3. Comparación con los objetivos planteados

| Objetivo específico | Estado | Evidencia |
|---|---|---|
| 1. API RESTful con Spring Boot | Cumplido | 69 endpoints en 11 controladores |
| 2. Arquitectura por capas con inyección por constructor | Cumplido | Siete paquetes, ningún `@Autowired` en campos |
| 3. TDD con JUnit 5 y MockMvc | Cumplido | 156 pruebas, 0 fallos |
| 4. Validación con DTO y manejo global de errores | Cumplido | Bean Validation y `ApiExceptionHandler` |
| 5. Front-end Angular que consume la API | Cumplido, por adelantado | 13 pantallas operativas |
| 6. Integración back-end y front-end | Cumplido | CORS configurado, contrato de datos compartido |
| 7. Trabajo colaborativo en GitHub | Cumplido | Seis ramas de funcionalidad, integración por Pull Request |
| 8. Preparación para BD, seguridad y despliegue | En curso | Modelo de dominio definido, repositorios aislados, paquete `security` reservado |

Los objetivos 1 a 4 corresponden al alcance de las semanas 1 a 4 y se cumplieron en su totalidad. Los objetivos 5 y 6 se adelantaron respecto del cronograma. El objetivo 8 permanece en curso por corresponder a unidades aún no dictadas.

[SALTO]

# 9. Conclusiones

1. **Se implementó una API RESTful completa y funcional con Spring Boot**, con 69 endpoints que cubren los diez módulos del dominio odontológico, aplicando de forma consistente el diseño orientado a recursos, los verbos HTTP y los códigos de estado del protocolo.

2. **La arquitectura por capas con inyección por constructor demostró su valor de forma concreta.** Al mantener las reglas de negocio en la capa de servicio y el acceso a datos detrás de una interfaz de repositorio, el cambio de persistencia en memoria a base de datos relacional quedó acotado a una sola capa. La decisión de diseño tomada en la semana 2 es la que permitirá que la semana 6 no obligue a reescribir nada.

3. **El desarrollo guiado por pruebas cambió la forma de trabajar del equipo, no solo el resultado.** Escribir primero la prueba obligó a definir el comportamiento esperado antes que la implementación, lo que hizo aflorar reglas de negocio ambiguas —qué ocurre al pagar más del saldo, o al reprogramar una cita cancelada— cuando aún eran baratas de resolver. Las 156 pruebas verdes son, además, la condición que permite refactorizar sin miedo.

4. **Centralizar el manejo de errores tuvo un efecto que excedió al back-end.** Una única estructura de error para toda la API permitió que el front-end la tradujera en un solo servicio, y que ningún componente de Angular tenga que interpretar códigos de estado HTTP.

5. **Respetar el alcance de cada etapa resultó ser una decisión acertada.** Renunciar deliberadamente a JPA y a Spring Security en las primeras semanas mantuvo el proyecto dentro de lo estudiado y evitó acumular complejidad sobre bases aún no comprendidas. Documentar por escrito lo que falta —en el `pom.xml`, en el README y en este informe— resultó más honesto y más útil que simularlo.

6. **El trabajo colaborativo exigió acuerdos previos, no solo herramientas.** La división por módulos y la coincidencia en los nombres de entidades, DTO y rutas evitaron la mayor parte de los conflictos; GitHub y el flujo de Pull Request resolvieron el resto.

**Aprendizajes obtenidos:** el equipo consolidó el modelo de programación de Spring Boot, comprendió la inversión de control como una decisión de diseño y no como una anotación, incorporó el ciclo Red–Green–Refactor como hábito de trabajo, y aprendió que la integración entre dos aplicaciones se decide antes de escribirlas, en el contrato de datos que ambas aceptan.

[SALTO]

# 10. Recomendaciones

## 10.1. Mejoras posibles para la aplicación

1. **Completar la persistencia con JPA e Hibernate**, con `@Transactional` en las operaciones que abarcan más de una entidad —el registro de un pago y la actualización del saldo del tratamiento es el caso prioritario— e índices sobre los campos de búsqueda frecuente: documento del paciente, fecha de la cita y paciente del tratamiento.
2. **Implementar Spring Security con JWT**, cifrar las contraseñas con BCrypt —hoy se comparan en texto plano, lo que es aceptable solo como paso intermedio del aprendizaje— y aplicar autorización por rol con `@PreAuthorize`, sustituyendo el `usuarioId` que hoy viaja como parámetro por la identidad extraída del token.
3. **Añadir paginación y ordenamiento** en los listados que crecerán con el uso: pacientes, citas y pagos. Devolver la colección completa deja de ser viable con volúmenes reales.
4. **Documentar la API con OpenAPI/Swagger**, generando la especificación desde el código para que se mantenga sincronizada de forma automática.
5. **Incorporar pruebas unitarias puras de los servicios con Mockito**, complementando las actuales pruebas de integración con MockMvc. Ejecutan más rápido y localizan el fallo con mayor precisión.
6. **Añadir pruebas automatizadas del front-end** con Vitest para la lógica de los servicios y pruebas de extremo a extremo para los flujos principales.
7. **Registrar una bitácora de auditoría** de las operaciones sobre información clínica: quién registró, modificó o anuló cada dato y cuándo.
8. **Migrar el almacenamiento de archivos clínicos a la nube**, sustituyendo la carpeta local por un servicio de objetos con acceso restringido.

## 10.2. Tecnologías alternativas o complementarias

- **Contenedores** para eliminar las diferencias entre entornos y describir el conjunto —back-end, front-end y base de datos— en un solo archivo de composición.
- **Flyway o Liquibase** para versionar el esquema de la base de datos con la misma disciplina con que se versiona el código.
- **Redis** como caché de los indicadores del panel, que hoy se recalculan en cada solicitud.
- **Angular Material o PrimeNG** para componentes de interfaz probados en accesibilidad, especialmente en el calendario de la agenda.
- **SonarQube** integrado en la tubería de CI para medir la cobertura de pruebas y detectar deuda técnica.

## 10.3. Buenas prácticas para el desarrollo de aplicaciones web

- Mantener los controladores delgados: traducen HTTP, no deciden reglas de negocio.
- Preferir la inyección por constructor sobre la inyección en campos, con dependencias declaradas como `final`.
- No exponer nunca las entidades del dominio al exterior: usar DTO de entrada y de salida.
- Validar siempre en el servidor, aunque el cliente ya haya validado.
- Devolver una estructura de error única y estable en toda la API.
- Escribir la prueba antes que el código y no dar por terminada una regla de negocio sin al menos una prueba que la respalde.
- Externalizar la configuración que cambia entre entornos, y nunca versionar credenciales.
- Trabajar con ramas por funcionalidad e integrar mediante Pull Request revisado por otra persona.

[SALTO]

# 11. Referencias Bibliográficas

Beck, K. (2003). *Test-driven development: By example*. Addison-Wesley.

Díaz Marcos, A. (2019). *Desarrollo de una aplicación web con Spring Boot y Angular para la gestión de un catálogo de productos* [Trabajo de fin de grado, Universidad Politécnica de Madrid]. Archivo Digital UPM. https://oa.upm.es/56186/

Fielding, R. T. (2000). *Architectural styles and the design of network-based software architectures* [Tesis doctoral, University of California, Irvine]. https://ics.uci.edu/~fielding/pubs/dissertation/top.htm

Google LLC. (2026). *Angular documentation*. https://angular.dev/

Martin, R. C. (2017). *Clean architecture: A craftsman's guide to software structure and design*. Prentice Hall.

Mosquera Pérez, J. M., y Mauro Silva, W. A. (2024). *Sistema web de gestión de citas e historias clínicas para el centro odontológico especializado Disan Cuñumbuqui – San Martín 2023* [Tesis de pregrado, Universidad Nacional de la Amazonía Peruana]. Repositorio Institucional UNAP. https://hdl.handle.net/20.500.12737/10225

Richardson, L., y Ruby, S. (2007). *RESTful web services*. O'Reilly Media.

Telenchana Chimbo, D. I. (2022). *Aplicación web usando el framework Angular para el control de historias clínicas de los pacientes del consultorio médico Fisio&Trauma de la ciudad de Ambato* [Tesis de pregrado, Universidad Técnica de Ambato]. Repositorio UTA. https://repositorio.uta.edu.ec/handle/123456789/34812

VMware Tanzu. (2026). *Spring Boot reference documentation*. https://docs.spring.io/spring-boot/

Walls, C. (2022). *Spring in action* (6.ª ed.). Manning Publications.

[SALTO]

# 12. Anexos

## 12.1. Código Fuente

### A. Controlador REST con inyección por constructor

`backend/src/main/java/com/utp/odontologia/controller/PacienteController.java`

```java
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    // Inyeccion por constructor: el campo es final y la dependencia es explicita.
    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /** GET /api/pacientes?texto=&estado= - listado resumido con filtros opcionales. */
    @GetMapping
    public ResponseEntity<List<PacienteResumenResponse>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoPaciente estado) {

        List<PacienteResumenResponse> respuesta = pacienteService.listar(texto, estado)
                .stream()
                .map(PacienteResumenResponse::desde)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    /** GET /api/pacientes/{id} - ficha completa del paciente. */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PacienteResponse.desde(pacienteService.buscarPorId(id)));
    }

    /** POST /api/pacientes - registra un paciente y devuelve 201 con Location. */
    @PostMapping
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) {
        Paciente creado = pacienteService.crear(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(PacienteResponse.desde(creado));
    }

    /** DELETE /api/pacientes/{id} - elimina al paciente. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### B. DTO de entrada con validaciones declarativas

`backend/src/main/java/com/utp/odontologia/dto/PacienteRequest.java`

```java
public record PacienteRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
        String dni,

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El sexo es obligatorio")
        @Pattern(regexp = "M|F", message = "El sexo debe ser M o F")
        String sexo,

        @Email(message = "El email no tiene un formato valido")
        String email,

        @Valid AntecedentesRequest antecedentes) {
}
```

### C. Servicio con reglas de negocio

`backend/src/main/java/com/utp/odontologia/service/PacienteService.java`

```java
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /** Busca un paciente por id o falla con 404. */
    public Paciente buscarPorId(Long id) {
        return pacienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", id));
    }

    /** Registra un paciente nuevo. El DNI no puede repetirse. */
    public Paciente crear(PacienteRequest request) {
        validarDniDisponible(request.dni(), null);

        Paciente paciente = new Paciente();
        copiarDatos(request, paciente);
        return pacienteRepository.guardar(paciente);
    }
}
```

### D. Manejo centralizado de excepciones

`backend/src/main/java/com/utp/odontologia/exception/ApiExceptionHandler.java`

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 404: el recurso solicitado no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 400: se incumple una regla de negocio. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 400: fallo una validacion declarada en el DTO. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validaciones(MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(estado.value(), mensaje, request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }
}
```

### E. Repositorio en memoria preparado para migrar a JPA

`backend/src/main/java/com/utp/odontologia/repository/RepositorioEnMemoria.java`

```java
public abstract class RepositorioEnMemoria<T> {

    protected final Map<Long, T> datos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    protected abstract Long obtenerId(T entidad);
    protected abstract void asignarId(T entidad, Long id);

    /** Inserta la entidad si no tiene id, o la reemplaza si ya lo tiene. */
    public T guardar(T entidad) {
        if (obtenerId(entidad) == null) {
            asignarId(entidad, secuencia.getAndIncrement());
        }
        datos.put(obtenerId(entidad), entidad);
        return entidad;
    }

    public Optional<T> buscarPorId(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(datos.get(id));
    }

    public List<T> listarSi(Predicate<T> filtro) {
        return listar().stream().filter(filtro).toList();
    }

    /** Vacia el repositorio. Se usa para dejar las pruebas en un estado conocido. */
    public void limpiar() {
        datos.clear();
        secuencia.set(1);
    }
}
```

### F. Prueba de integración con MockMvc (TDD)

`backend/src/test/java/com/utp/odontologia/PacienteControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @BeforeEach
    void prepararDatos() {
        pacienteRepository.limpiar();
        ana = pacienteRepository.guardar(nuevo("12345678", "Ana Maria", "Torres Vega", ...));
        luis = pacienteRepository.guardar(nuevo("87654321", "Luis Alberto", "Ramirez Soto", ...));
        carla = pacienteRepository.guardar(nuevo("11223344", "Carla", "Mendoza Rios", ...));
    }

    @Test
    @DisplayName("POST /api/pacientes devuelve 201 y la cabecera Location")
    void crearPaciente() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("99887766")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.dni").value("99887766"));
    }

    @Test
    @DisplayName("POST /api/pacientes rechaza un DNI repetido con 400")
    void rechazarDniRepetido() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("12345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("DNI")));
    }
}
```

### G. Cliente HTTP único del front-end

`frontend/src/app/core/services/api.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = environment.apiUrl;

  get<T>(ruta: string, params?: ParamsHttp): Observable<T> {
    return this.http
      .get<T>(this.url(ruta), { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  post<T>(ruta: string, cuerpo?: unknown, params?: ParamsHttp): Observable<T> {
    return this.http
      .post<T>(this.url(ruta), cuerpo ?? {}, { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  /** Normaliza cualquier fallo a la estructura ErrorApi que usa toda la aplicacion. */
  private traducirError(fallo: HttpErrorResponse, ruta: string): Observable<never> {
    if (fallo.status === 0) {
      return throwError(() => ({
        estado: 0,
        mensaje: 'No se pudo conectar con el servidor',
        ruta,
        fechaHora: new Date().toISOString().slice(0, 19),
      }) satisfies ErrorApi);
    }
    // Si el back-end respondio con su JSON de error, se respeta su mensaje.
    ...
  }
}
```

### H. Rutas con carga diferida y guardia de acceso

`frontend/src/app/app.routes.ts` y `frontend/src/app/core/guards/auth-guard.ts`

```typescript
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'login',
    loadComponent: () => import('./paginas/login/login').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.LayoutComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./paginas/dashboard/dashboard')
          .then((m) => m.DashboardComponent) },
      { path: 'pacientes', loadComponent: () => import('./paginas/pacientes/pacientes')
          .then((m) => m.PacientesComponent) },
      // ... once pantallas mas
    ],
  },
];

export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { retorno: estado.url } });
};
```

## 12.2. Configuraciones

### A. Dependencias del back-end — `backend/pom.xml`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.6</version>
</parent>

<properties>
    <java.version>25</java.version>
</properties>

<dependencies>
    <!-- Endpoints REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- @Valid y anotaciones de validacion -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JUnit 5, MockMvc, Mockito, AssertJ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!--
        NOTA DE ALCANCE (semanas 1 a 4 del curso):
        Todavia NO se incluyen spring-boot-starter-data-jpa, el driver de
        la base de datos ni spring-boot-starter-security. Segun el plan de
        trabajo esas dependencias entran en las semanas 6 a 10.
    -->
</dependencies>
```

### B. Configuración de la aplicación — `backend/src/main/resources/application.properties`

```properties
spring.application.name=odontologia-backend
server.port=8080

# Mensajes de error detallados durante el desarrollo.
server.error.include-message=always
server.error.include-binding-errors=always

# Limites para la subida de archivos clinicos.
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=12MB

# Carpeta local donde se guardan los archivos clinicos durante el desarrollo.
app.archivos.directorio=uploads

# Origen permitido para el frontend Angular en desarrollo.
app.cors.origenes=http://localhost:4200
```

### C. Configuración CORS — `backend/src/main/java/com/utp/odontologia/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] origenes;

    public CorsConfig(@Value("${app.cors.origenes}") String origenes) {
        this.origenes = origenes.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location");   // el cliente necesita leer la URI creada
    }
}
```

### D. Configuración del front-end — `frontend/src/environments/environment.ts`

```typescript
export const environment = {
  produccion: true,
  apiUrl: 'http://localhost:8080/api',
};
```

### E. Dependencias del front-end — `frontend/package.json`

```json
{
  "dependencies": {
    "@angular/common": "^21.2.0",
    "@angular/compiler": "^21.2.0",
    "@angular/core": "^21.2.0",
    "@angular/forms": "^21.2.0",
    "@angular/platform-browser": "^21.2.0",
    "@angular/router": "^21.2.0",
    "rxjs": "~7.8.0"
  },
  "devDependencies": {
    "@angular/cli": "^21.2.10",
    "typescript": "~5.9.2",
    "vitest": "^4.0.8"
  }
}
```

## 12.3. Capturas de Pantalla

### A. Interfaz de usuario en funcionamiento

Las siguientes capturas corresponden a la aplicacion en ejecucion, con el front-end Angular en `http://localhost:4200` consumiendo la API de Spring Boot en `http://localhost:8080/api`.

[IMG:capturas/01-login.png]
[PIE:Figura 1. Pantalla de inicio de sesion con validacion de formulario reactivo.]

[IMG:capturas/02-dashboard.png]
[PIE:Figura 2. Panel de indicadores: pacientes registrados, citas del dia, tratamientos activos, pagos pendientes e ingresos del mes.]

[IMG:capturas/03-pacientes.png]
[PIE:Figura 3. Listado de pacientes con busqueda por nombre o documento y filtro por estado.]

[IMG:capturas/04-paciente-ficha.png]
[PIE:Figura 4. Ficha del paciente con sus antecedentes, citas, tratamientos y archivos.]

[IMG:capturas/05-citas.png]
[PIE:Figura 5. Agenda del dia y gestion de citas: confirmacion, asistencia, cancelacion y reprogramacion.]

[IMG:capturas/06-historias.png]
[PIE:Figura 6. Historia clinica: registro de consultas y consulta del historial, sin sobrescritura.]

[IMG:capturas/07-odontograma.png]
[PIE:Figura 7. Odontograma con la notacion FDI y el historial por pieza dental.]

[IMG:capturas/08-tratamientos.png]
[PIE:Figura 8. Tratamientos con sus sesiones, estado y saldo pendiente.]

[IMG:capturas/09-pagos.png]
[PIE:Figura 9. Registro de pagos y estado de cuenta del paciente.]

[IMG:capturas/10-archivos.png]
[PIE:Figura 10. Archivos clinicos: subida multipart, filtros por tipo y descarga.]

[IMG:capturas/11-reportes.png]
[PIE:Figura 11. Reportes de pacientes, citas, tratamientos, ingresos y pagos pendientes.]

[IMG:capturas/12-usuarios.png]
[PIE:Figura 12. Administracion de usuarios y roles.]

### B. Pruebas de la API REST en ejecucion

Las siguientes ejecuciones se realizaron con `curl` contra el servidor en funcionamiento; las mismas solicitudes se verificaron con Postman. Se incluyen tanto casos correctos como casos de error, para evidenciar el funcionamiento del manejador global de excepciones.

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



## 12.4. Manual de Usuario

### A. Requisitos previos

- **Java 25** o superior (JDK).
- **Node.js 20** o superior, con npm.
- **Maven**, o el wrapper `mvnw` incluido en el proyecto.
- Un navegador web actual.

### B. Puesta en marcha

**1. Levantar el back-end.** Desde la carpeta `ProyectoFinal/backend`:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080/api`. Al arrancar se cargan automáticamente los datos de demostración: usuarios, pacientes, citas, historias clínicas, registros de odontograma, tratamientos y pagos.

**2. Levantar el front-end.** Desde la carpeta `ProyectoFinal/frontend`, en una segunda terminal:

```bash
npm install
npm start
```

La aplicación queda disponible en `http://localhost:4200`. El back-end debe estar en ejecución para que las pantallas muestren información.

**3. Ejecutar las pruebas.** Desde `ProyectoFinal/backend`:

```bash
mvn test
```

### C. Ingreso al sistema

Abrir `http://localhost:4200` en el navegador. La aplicación redirige a la pantalla de inicio de sesión. Credenciales de demostración:

```
usuario: admin
clave:   admin123
```

### D. Uso de los módulos

**Panel de indicadores (Dashboard).** Es la pantalla inicial tras ingresar. Muestra pacientes registrados, citas del día, citas pendientes, tratamientos activos, pagos pendientes e ingresos del mes, además de las próximas citas, los pacientes recientes y las alertas del sistema.

**Pacientes.** El listado permite buscar por nombre o por documento y filtrar por estado. El botón de registro abre el formulario de alta: el documento debe tener ocho dígitos y no puede repetirse; la edad se calcula automáticamente. Al seleccionar un paciente se abre su ficha, con sus antecedentes médicos y odontológicos, sus citas, sus tratamientos y sus archivos.

**Citas.** Muestra la agenda del día seleccionado. Para registrar una cita se eligen paciente, odontólogo, fecha y hora; el sistema rechaza el registro si el odontólogo ya tiene una cita en ese horario. Desde el listado se confirma la cita, se registra la asistencia, se cancela indicando el motivo o se reprograma, lo que crea una cita nueva vinculada a la original.

**Historia clínica.** Registra una consulta con motivo, anamnesis, examen clínico, diagnóstico, procedimiento, medicamentos y observaciones. Las consultas anteriores nunca se modifican ni se eliminan: una consulta registrada por error se anula indicando el motivo y permanece visible en el historial.

**Odontograma.** Presenta el estado actual de todas las piezas dentales del paciente según la notación FDI. Al registrar un estado nuevo —caries, obturado, endodoncia, corona, extracción indicada, entre otros— se crea un registro adicional y el anterior se conserva, de modo que puede consultarse el historial completo de cada pieza.

**Tratamientos.** Registra el tratamiento con su descripción, precio y estado, y permite añadir las sesiones necesarias y marcarlas como realizadas. Un tratamiento completado o cancelado ya no admite cambios ni sesiones nuevas.

**Pagos.** Registra pagos totales o parciales sobre un tratamiento. El sistema no acepta un monto superior al saldo pendiente. El estado de cuenta del paciente muestra el total, lo pagado y el saldo.

**Archivos clínicos.** Permite subir radiografías, fotografías, informes, consentimientos y documentos, con un máximo de diez megabytes por archivo y solo con las extensiones permitidas. Los archivos se listan por paciente y por tipo, y se descargan desde el listado.

**Reportes.** Genera reportes de pacientes registrados, citas por período agrupadas por estado, tratamientos realizados y pendientes, ingresos por método de pago y por mes, pagos pendientes e historial consolidado de un paciente. Las fechas se indican en formato `aaaa-mm-dd`.

**Usuarios.** Disponible para el rol administrador. Permite registrar usuarios, asignar el rol —administrador, odontólogo, recepcionista o asistente—, cambiar la contraseña y activar o desactivar cuentas. La contraseña nunca se muestra en pantalla ni se devuelve en las respuestas de la API.

### E. Consideraciones importantes

- **Los datos no se conservan al reiniciar el servidor.** En esta etapa el almacenamiento es en memoria; la persistencia en base de datos corresponde a una unidad posterior del curso.
- **La sesión no está protegida con seguridad real.** El control de acceso del front-end mejora la experiencia de uso, pero los endpoints aún no exigen credenciales. Esta protección se incorporará junto con Spring Security y JWT.
- **El back-end debe estar en ejecución** antes de usar el front-end. Si no lo está, la aplicación muestra el mensaje «No se pudo conectar con el servidor».

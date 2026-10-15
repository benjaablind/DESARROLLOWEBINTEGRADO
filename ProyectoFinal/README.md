# Sistema de Gestión Odontológica

Proyecto final del curso Desarrollo Web Integrado (100000ST61). Aplicación de tres capas: API REST con Spring Boot, base de datos MySQL y frontend con Angular.

## Alcance actual

El desarrollo sigue las etapas del curso y el plan de trabajo del equipo, que están en [Plan_de_trabajo_6_integrantes_Sistema_Odontologico.docx](Plan_de_trabajo_6_integrantes_Sistema_Odontologico.docx).

Lo implementado corresponde a las semanas 1 a 10, más el avance adicional del frontend:

| Etapa | Contenido | Estado |
|-------|-----------|--------|
| Semanas 1 a 4 | Spring Boot, API REST, controladores, servicios, inyección de dependencias, DTOs, validaciones, manejo de excepciones y pruebas con JUnit y MockMvc | Implementado |
| Semanas 6 a 10 | MySQL, JPA, Hibernate, CRUD, JPQL, transacciones, Spring Security, roles y JWT | Implementado |
| Semanas 11 a 15 | Angular, componentes, SCSS, rutas, formularios y consumo de la API | Adelantado; falta adaptarlo al JWT |
| Semanas 16 a 18 | Integración final, optimización y despliegue | Pendiente |

### Qué cambió en las semanas 6 a 10

- **Persistencia en MySQL con JPA e Hibernate (semana 6).** Las 10 clases del modelo son entidades; Hibernate genera 14 tablas con 21 claves foráneas. Los repositorios extienden `RepositorioBase`, que sobre `JpaRepository` conserva los nombres que ya usaban los servicios (`guardar`, `buscarPorId`, `listar`...): **los servicios no se reescribieron**, tal como prometía la arquitectura por capas.
- **JPQL y transacciones (semana 7).** Las búsquedas que filtraban en memoria son consultas derivadas o `@Query` con JPQL, incluidas agregaciones (`SUM`, `GROUP BY`) en reportes y pagos. Cada servicio delimita su transacción con `@Transactional` (las consultas, `readOnly`). Reprogramar una cita es atómico, y registrar un pago bloquea la fila del tratamiento para que dos cobros simultáneos no superen el saldo. Los importes pasaron de `double` a `BigDecimal` (`DECIMAL(12,2)`).
- **Spring Security con roles (semana 8).** Contraseñas con BCrypt. La matriz de permisos por rol, que antes el servidor solo informaba, ahora la aplica `SecurityConfig`.
- **JWT (semana 9).** El login emite un token firmado (HS256) y cada petición lo presenta como `Authorization: Bearer`. El servidor ya no confía en un `usuarioId` enviado por el cliente: la identidad sale del token.
- Los archivos clínicos siguen en la carpeta local `uploads/` (en la base solo van sus metadatos) y ahora **solo los ven el administrador y el odontólogo**.

Se eligió **MySQL** en lugar del PostgreSQL que mencionaba el plan inicial, por coherencia con los laboratorios del curso. El código JPA no depende del motor: cambiarlo es cuestión de driver y URL.

## Estructura

```
ProyectoFinal/
├── backend/          API REST con Spring Boot
└── frontend/         Aplicación Angular
```

El backend está organizado por capas, como indica el plan de trabajo:

```
src/main/java/com/utp/odontologia/
├── config/           Seguridad, JWT, CORS y datos de demostración
├── controller/       Endpoints REST de cada módulo
├── dto/              Objetos de entrada y salida, con validaciones
├── exception/        Excepciones propias y manejador global
├── model/            Entidades JPA del dominio
├── repository/       Spring Data JPA: consultas derivadas y JPQL
├── security/         Emisión del JWT, usuarios desde la base, usuario actual
└── service/          Reglas de negocio y transacciones
```

El frontend es una aplicación Angular con componentes independientes y señales:

```
src/app/
├── core/
│   ├── models/       Interfaces y tipos que reflejan los DTO del backend
│   ├── services/     Un servicio por módulo, sobre un cliente HTTP común
│   └── guards/       Control de acceso a las rutas
├── layout/           Barra lateral, barra superior y notificaciones
└── paginas/          Una carpeta por pantalla
```

Pantallas disponibles: inicio de sesión, panel de indicadores, pacientes con su ficha, citas con agenda del día, historia clínica, odontograma, tratamientos, pagos, archivos, reportes y usuarios.

## Reparto por integrante

| Integrante | Módulo | Rama |
|-----------|--------|------|
| 1 | Usuarios, roles y autenticación | `feature/usuarios` |
| 2 | Pacientes | `feature/pacientes` |
| 3 | Citas, agenda y dashboard | `feature/citas-dashboard` |
| 4 | Historia clínica y odontograma | `feature/historia-odontograma` |
| 5 | Tratamientos y pagos | `feature/tratamientos-pagos` |
| 6 | Archivos clínicos y reportes | `feature/archivos-reportes` |

La documentación completa de los endpoints está en [backend/DOCUMENTACION_API.md](backend/DOCUMENTACION_API.md).

Para probar la API sin escribir una sola línea, importa la colección de [postman/](postman/): 73 solicitudes organizadas por módulo, con el caso correcto y el caso de error de cada regla de negocio. La primera inicia sesión y guarda el token que usan todas las demás.

**Avance 2 (semanas 6 a 10):** qué explica cada integrante, el orden de la demostración y las preguntas probables están en [GUIA_AVANCE_2.md](GUIA_AVANCE_2.md).

El reparto de la sustentación del Avance 1, el orden de la demostración y las respuestas a las preguntas más probables están en [GUIA_SUSTENTACION.md](GUIA_SUSTENTACION.md).

El flujo detallado de cada integrante —su commit, su código, su prueba y sus solicitudes de Postman— está en [FLUJO_POR_INTEGRANTE.md](FLUJO_POR_INTEGRANTE.md).

## Cómo ejecutar

Necesitas **Java 25**, **MySQL 8** y Node 20 o superior. Maven no hace falta instalarlo: el proyecto trae el wrapper, `mvnw` en Git Bash y `mvnw.cmd` en PowerShell o CMD.

**1. Base de datos, una sola vez.** En MySQL Workbench ejecuta [backend/sql/01_crear_base_datos.sql](backend/sql/01_crear_base_datos.sql). Las tablas no se crean a mano: las genera Hibernate al arrancar.

> **Con XAMPP.** XAMPP trae MariaDB 10.4, compatible con el driver de MySQL; la API se probó completa contra él (14 tablas, 21 claves foráneas, la colección de Postman sin fallas).
>
> 1. En el **XAMPP Control Panel**, pulsa **Start** en la fila *MySQL*.
> 2. Crea la base desde la terminal: `C:\xampp\mysql\bin\mysql.exe -u root < backend\sql\01_crear_base_datos.sql`. O en phpMyAdmin (`http://localhost/phpmyadmin`, con Apache encendido), pestaña **SQL**, pega el script y pulsa **Continuar**.
> 3. Al arrancar el backend, cuando pida la contraseña de MySQL, pulsa **Enter**: el `root` de XAMPP no tiene contraseña.
>
> Al iniciar aparece el aviso `HHH000511: The 5.5.5 version for [MySQLDialect] is no longer supported`. Es esperado: MariaDB se presenta como "5.5.5" ante el driver de MySQL. Las tablas, las claves foráneas y las consultas funcionan.

**2. Backend**, desde `backend/`:

```powershell
.\iniciar.ps1      # PowerShell
./iniciar.sh       # Git Bash, Linux o macOS
```

La aplicación necesita dos secretos que **no están en el repositorio**, y el script los deja solo en esa sesión de terminal:

| Variable | Qué es | Cómo la obtiene el script |
|----------|--------|---------------------------|
| `DB_PASSWORD` | Contraseña de MySQL (usuario `DB_USERNAME`, por defecto `root`) | La pide por teclado si no está definida |
| `JWT_SECRET_BASE64` | Clave con la que se firman los tokens | Genera 32 bytes aleatorios |

Sin `JWT_SECRET_BASE64` la aplicación no arranca, y es a propósito: con esa clave cualquiera podría fabricar un token de administrador. Cada arranque genera una clave nueva, así que los tokens anteriores dejan de servir y hay que volver a iniciar sesión.

Con un JDK anterior al 25 la compilación falla con `release version 25 not supported`. Como salida temporal se puede añadir `-Djava.version=24`, ajustando el número al JDK instalado; en PowerShell hay que escribirlo entre comillas: `.\mvnw.cmd "-Djava.version=24" spring-boot:run`.

Queda disponible en `http://localhost:8080/api`. El primer arranque carga en la base una clínica de ejemplo: usuarios, pacientes, citas, historias, odontograma, tratamientos y pagos. Los siguientes no tocan nada, porque los datos ya persisten. [backend/sql/02_verificar_base_datos.sql](backend/sql/02_verificar_base_datos.sql) muestra las tablas, las claves foráneas y que las contraseñas están cifradas.

**3. Frontend**, desde `frontend/`:

```bash
npm install
npm start
```

Queda disponible en `http://localhost:4200` y consume la API del puerto 8080.

> **Pendiente para las semanas 11 a 15:** el frontend todavía no envía el token. Mientras no se le agregue un interceptor que adjunte `Authorization: Bearer`, el backend le responde 401. Por ahora la API se prueba con Postman.

Usuarios de demostración, uno por rol:

| Usuario | Clave | Rol | Puede |
|---------|-------|-----|-------|
| `admin` | `admin123` | ADMINISTRADOR | Todo |
| `lramos` | `lramos123` | ODONTOLOGO | Pacientes, citas, historias, odontograma, tratamientos y archivos |
| `rdiaz` | `rdiaz123` | RECEPCIONISTA | Pacientes, citas, pagos y reportes |
| `jpalma` | `jpalma123` | ASISTENTE | Consultar pacientes y citas |

## Pruebas

Desde `backend/`:

```bash
./mvnw test      # Git Bash
.\mvnw.cmd test  # PowerShell
```

Son 176 pruebas de integración con JUnit 5 y MockMvc. No necesitan MySQL: usan una base H2 en memoria, en modo compatible con MySQL.

- Las 156 originales, una clase por módulo, ahora corren sobre la base de datos.
- `TransaccionesYPersistenciaTest` (3): reprogramar una cita hace rollback completo si falla el segundo paso, y borrar un paciente con historial responde 409.
- `SeguridadTest` (17): login real con BCrypt y JWT, token alterado o vencido, y la matriz de permisos de cada rol.

## Reglas de trabajo del equipo

- Nadie modifica `main` directamente. Todo entra por Pull Request y con revisión de otro integrante.
- Cada integrante trabaja en la rama de su módulo y hace sus propios commits, para que su avance quede registrado a su nombre.
- Los mensajes de commit describen el cambio, por ejemplo `feat: implementar CRUD de pacientes`.
- Los nombres de entidades, DTOs y endpoints se mantienen consistentes entre módulos para que la integración no genere conflictos.

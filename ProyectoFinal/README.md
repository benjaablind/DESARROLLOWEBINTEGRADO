# Sistema de Gestión Odontológica

Proyecto final del curso Desarrollo Web Integrado (100000ST61). Aplicación de tres capas: API REST con Spring Boot, frontend con Angular y, en una etapa posterior, PostgreSQL.

## Alcance actual

El desarrollo sigue las etapas del curso y el plan de trabajo del equipo, que están en [Plan_de_trabajo_6_integrantes_Sistema_Odontologico.docx](Plan_de_trabajo_6_integrantes_Sistema_Odontologico.docx).

Lo implementado corresponde a las semanas 1 a 4, más el avance adicional del frontend:

| Etapa | Contenido | Estado |
|-------|-----------|--------|
| Semanas 1 a 4 | Spring Boot, API REST, controladores, servicios, inyección de dependencias, DTOs, validaciones, manejo de excepciones y pruebas con JUnit y MockMvc | Implementado |
| Semanas 6 a 10 | PostgreSQL, JPA, Hibernate, Spring Security, roles y JWT | Pendiente por etapa del curso |
| Semanas 11 a 15 | Angular, componentes, SCSS, rutas, formularios y consumo de la API | Adelantado en este avance |
| Semanas 16 a 18 | Integración final, optimización y despliegue | Pendiente |

Decisiones que respetan el alcance, tomadas a propósito:

- La persistencia es **en memoria**. No hay JPA, ni Hibernate, ni ninguna base de datos. Cada repositorio extiende una clase base que guarda las entidades en un mapa concurrente. Cuando llegue la unidad de base de datos, esos repositorios pasan a extender `JpaRepository` sin que los servicios cambien.
- **No hay Spring Security ni JWT**. El inicio de sesión compara credenciales en el servicio y el frontend guarda al usuario en el navegador. La protección real de endpoints entra en la etapa de seguridad.
- Los archivos clínicos se guardan en una carpeta local `uploads/`, con una estructura pensada para migrar a almacenamiento en la nube más adelante.

## Estructura

```
ProyectoFinal/
├── backend/          API REST con Spring Boot
└── frontend/         Aplicación Angular
```

El backend está organizado por capas, como indica el plan de trabajo:

```
src/main/java/com/utp/odontologia/
├── config/           CORS y datos de demostración
├── controller/       Endpoints REST de cada módulo
├── dto/              Objetos de entrada y salida, con validaciones
├── exception/        Excepciones propias y manejador global
├── model/            Entidades del dominio
├── repository/       Almacenamiento en memoria
├── security/         Reservado para la etapa de seguridad
└── service/          Reglas de negocio
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

Para probar la API sin escribir una sola línea, importa la colección de [postman/](postman/): 65 solicitudes organizadas por módulo, con el caso correcto y el caso de error de cada regla de negocio.

El reparto de la sustentación, el orden de la demostración y las respuestas a las preguntas más probables están en [GUIA_SUSTENTACION.md](GUIA_SUSTENTACION.md).

## Cómo ejecutar

Necesitas **Java 25** y Node 20 o superior. Maven no hace falta instalarlo: el proyecto trae el wrapper, `mvnw` en Git Bash y `mvnw.cmd` en PowerShell o CMD.

**Backend**, desde `backend/`:

```bash
./mvnw spring-boot:run      # Git Bash
.\mvnw.cmd spring-boot:run  # PowerShell
```

Con un JDK anterior al 25 la compilación falla con `release version 25 not supported`. Como salida temporal se puede añadir `-Djava.version=24`, ajustando el número al JDK instalado; en PowerShell hay que escribirlo entre comillas: `.\mvnw.cmd "-Djava.version=24" spring-boot:run`.

Queda disponible en `http://localhost:8080/api`. Al arrancar carga datos de demostración: usuarios, pacientes, citas, historias, odontograma, tratamientos y pagos.

**Frontend**, desde `frontend/`:

```bash
npm install
npm start
```

Queda disponible en `http://localhost:4200` y consume la API del puerto 8080.

Credenciales de demostración:

```
usuario: admin
clave:   admin123
```

## Pruebas

Desde `backend/`:

```bash
./mvnw test      # Git Bash
.\mvnw.cmd test  # PowerShell
```

Son 156 pruebas de integración con JUnit 5 y MockMvc, repartidas en diez clases, una por módulo.

## Reglas de trabajo del equipo

- Nadie modifica `main` directamente. Todo entra por Pull Request y con revisión de otro integrante.
- Cada integrante trabaja en la rama de su módulo y hace sus propios commits, para que su avance quede registrado a su nombre.
- Los mensajes de commit describen el cambio, por ejemplo `feat: implementar CRUD de pacientes`.
- Los nombres de entidades, DTOs y endpoints se mantienen consistentes entre módulos para que la integración no genere conflictos.

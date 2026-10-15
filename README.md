# Desarrollo Web Integrado - CASOJ

Repositorio del curso Desarrollo Web Integrado.

## Proyecto Final: Sistema de Gestión Odontológica

Aplicación web de tres capas: API REST con Spring Boot, base de datos MySQL con JPA y frontend con Angular. La API está protegida con Spring Security y JWT. Está en [`ProyectoFinal/`](ProyectoFinal/).

- [Qué incluye y cómo ejecutarlo](ProyectoFinal/README.md)
- [Documentación de la API](ProyectoFinal/backend/DOCUMENTACION_API.md)
- [Colección de Postman](ProyectoFinal/postman/)
- [Guía del Avance 2 (semanas 6 a 10)](ProyectoFinal/GUIA_AVANCE_2.md)
- [Guía de sustentación del Avance 1](ProyectoFinal/GUIA_SUSTENTACION.md)

Para levantar la API (requiere MySQL con la base creada por `backend/sql/01_crear_base_datos.sql`):

```powershell
cd ProyectoFinal/backend
.\iniciar.ps1      # pide la clave de MySQL y genera la del JWT; API en http://localhost:8080/api
```

## Estructura

- `Laboratorio01/` a `Laboratorio18/` — un laboratorio por semana. Cada uno contiene:
  - `Integrante1/` a `Integrante6/` — una carpeta por cada integrante del grupo, donde cada uno avanza su propia solución.
  - Guías u otros materiales compartidos del laboratorio (cuando aplique) van sueltos en la raíz del laboratorio, ej. `Laboratorio03/Guia_Laboratorio_03_TDD_Spring_Boot.pdf`.
- `ProyectoFinal/` — proyecto final del curso.

# Desarrollo Web Integrado - CASOJ

Repositorio del curso Desarrollo Web Integrado.

## Proyecto Final: Sistema de Gestión Odontológica

Aplicación web de tres capas: API REST con Spring Boot, frontend con Angular y, en una etapa posterior, base de datos relacional. Está en [`ProyectoFinal/`](ProyectoFinal/).

- [Qué incluye y cómo ejecutarlo](ProyectoFinal/README.md)
- [Documentación de la API](ProyectoFinal/backend/DOCUMENTACION_API.md)
- [Colección de Postman](ProyectoFinal/postman/)
- [Guía de sustentación](ProyectoFinal/GUIA_SUSTENTACION.md)

Para levantarlo en dos terminales:

```bash
cd ProyectoFinal/backend && mvn spring-boot:run   # API en http://localhost:8080/api
cd ProyectoFinal/frontend && npm install && npm start   # UI en http://localhost:4200
```

## Estructura

- `Laboratorio01/` a `Laboratorio18/` — un laboratorio por semana. Cada uno contiene:
  - `Integrante1/` a `Integrante6/` — una carpeta por cada integrante del grupo, donde cada uno avanza su propia solución.
  - Guías u otros materiales compartidos del laboratorio (cuando aplique) van sueltos en la raíz del laboratorio, ej. `Laboratorio03/Guia_Laboratorio_03_TDD_Spring_Boot.pdf`.
- `ProyectoFinal/` — proyecto final del curso.

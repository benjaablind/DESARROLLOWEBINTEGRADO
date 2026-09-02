# Proyecto Spring Boot API REST - UTP

Este proyecto es una API REST completa para el control de tareas (`/api/tasks`), desarrollada con Spring Boot y lista para ejecutar en **Visual Studio Code**.

## 🚀 Pasos para ejecutar en Visual Studio Code

1. Extrae el archivo ZIP.
2. Abre **Visual Studio Code**.
3. Ve a **File > Open Folder...** y selecciona la carpeta descomprimida `spring-boot-rest-api`.
4. Asegúrate de tener instalada la extensión **"Extension Pack for Java"** de Microsoft.
5. Abre el archivo `src/main/java/com/utp/demo/DemoApplication.java` y haz clic en **Run** (o presiona `F5`).
6. El servidor se iniciará en `http://localhost:8080`.

## 📌 Endpoints disponibles

- **GET** `/api/tasks` -> Obtener todas las tareas.
- **GET** `/api/tasks/{id}` -> Obtener tarea por ID.
- **POST** `/api/tasks` -> Crear tarea (`{"title": "Nueva tarea", "completed": false}`).
- **PUT** `/api/tasks/{id}` -> Actualizar tarea completa.
- **PATCH** `/api/tasks/{id}` -> Actualizar parcialmente (`{"completed": true}`).
- **DELETE** `/api/tasks/{id}` -> Eliminar tarea.

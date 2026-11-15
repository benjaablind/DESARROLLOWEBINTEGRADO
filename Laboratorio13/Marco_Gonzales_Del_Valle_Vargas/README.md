# Laboratorio 13 — Marco Antonio Gonzales del Valle Vargas

**Formularios en Angular.** La aplicación `laboratorio-formularios` tiene dos vistas, según la
Guía de Laboratorio 13: un formulario de contacto **basado en plantillas** y un formulario de
registro **reactivo** con validadores propios, validación entre campos y controles dinámicos con
`FormArray`.

Los datos se muestran en consola y en la sección «Ver estado del formulario». No se envían a un
backend: el envío con `HttpClient` es el tema de la semana 14.

## Cómo ejecutarlo

```powershell
cd laboratorio-formularios
npm install
npm start          # http://localhost:4200/
npm test           # 24 pruebas unitarias (Vitest)
npx ng build       # build de produccion en dist/
```

> **Requisito de versión:** Angular 22 exige **Node.js 22.22.3+ o 24.15+**. El proyecto se
> generó y se probó con Node 24.22.3, npm 11.19.1, Angular CLI 22.2.7 y Angular 22.2.6.

El proyecto se creó con el comando de la sección 6.2, más dos opciones para que el CLI no haga
preguntas (`--ssr=false --ai-config=none`):

```powershell
ng new laboratorio-formularios --routing --style=css --standalone --strict --skip-git --file-name-style-guide=2025
ng generate component pages/formulario-plantilla --skip-tests
ng generate component pages/formulario-reactivo --skip-tests
```

## Estructura

```
laboratorio-formularios/src/app/
├── app.ts / app.html / app.css        cabecera con los dos enlaces + <router-outlet />
├── app.routes.ts                      '', plantilla, reactivo y '**', con loadComponent
├── validators/registro.validators.ts  edadMinima, passwordsIguales y dominioCorreo
└── pages/
    ├── formulario-plantilla/          FormsModule, [(ngModel)], #ref="ngModel", NgForm
    └── formulario-reactivo/           FormBuilder, nonNullable, FormArray y validador de grupo
```

## Los dos enfoques

| | Basado en plantillas | Reactivo |
|---|---|---|
| Dónde vive el modelo | En el HTML: cada `ngModel` con su `name` crea el `FormControl` | En TypeScript: `fb.group({...})` |
| Validación | Atributos: `required`, `minlength`, `email` | `Validators` y funciones `ValidatorFn` |
| Error entre campos | No se usa | `passwordsIgualesValidator` sobre el `FormGroup` |
| Controles dinámicos | No | `FormArray` de habilidades y de direcciones |

En los dos formularios, los mensajes aparecen solo cuando el control está `touched` o cuando se
intentó enviar (`submitted` / `markAllAsTouched()`), como pide la regla de experiencia de usuario
de la sección 4.2.

## Pruebas manuales (secciones 9.4 y 10.5)

Ejecutadas con Playwright contra `ng serve`. La consola no muestra errores.

| Prueba | Resultado |
|--------|-----------|
| Plantilla: enviar vacío | ✔ Errores en nombre, correo, mensaje y aceptación |
| Plantilla: nombre de 2 caracteres | ✔ «Ingrese al menos 3 caracteres.» |
| Plantilla: correo sin @ | ✔ «Ingrese un correo con formato válido.» |
| Plantilla: datos válidos | ✔ Mensaje de éxito y datos en consola |
| Reactivo: registrar vacío | ✔ Todo `touched` y con su mensaje |
| Reactivo: edad 16 | ✔ Error `edadMinima` |
| Reactivo: teléfono de 5 dígitos | ✔ Error `pattern` |
| Reactivo: contraseñas distintas | ✔ «Las contraseñas no coinciden.» bajo la confirmación |
| Reactivo: agregar dos habilidades | ✔ Tres controles; el botón Eliminar se desactiva con uno solo |
| Reactivo: datos válidos | ✔ Mensaje de éxito y objeto completo en consola |

## Evidencias (sección 13)

En [`evidencias/`](evidencias/):

| Archivo | Qué muestra |
|---------|-------------|
| `01_ng_version.png` | `ng version` con Angular 22.2.6 |
| `02_plantilla.png` | Formulario basado en plantillas |
| `03_plantilla_validaciones.png` | Mensajes de validación de la plantilla |
| `04_plantilla_enviado.png` | Envío válido y estado del formulario (válido, tocado, modificado) |
| `05_reactivo_errores.png` | Errores de edad mínima, contraseñas distintas, DNI, dominio y teléfono |
| `06_reactivo_habilidades_direcciones.png` | Tres habilidades dinámicas, dos direcciones y registro válido |
| `07_consola_datos_validos.png` | Datos válidos de ambos formularios en la consola |

La captura de consola reproduce los mensajes que Playwright leyó de la consola del navegador
durante la prueba. Son los mismos objetos que se ven en DevTools al ejecutar la aplicación.

## Actividades complementarias (sección 14)

**Actividad 1 — DNI peruano.** Campo `dni` con `Validators.required` y
`Validators.pattern(/^\d{8}$/)`. Tiene un mensaje para cada error: «El DNI es obligatorio.» y
«El DNI debe tener exactamente 8 dígitos.».

**Actividad 2 — Dominio del correo.** `dominioCorreoValidator('universidad.edu.pe')` devuelve
`{ dominioInvalido: true }` si el correo no termina en `@universidad.edu.pe` (no distingue
mayúsculas). Se suma a `Validators.email`, así que cada validador reporta su propio error. Por
eso el registro válido usa `marco@universidad.edu.pe`.

**Actividad 3 — Direcciones dinámicas.** `direcciones` es un `FormArray` de `FormGroup` con
departamento, provincia y dirección. En la plantilla se recorre con `formArrayName` y
`[formGroupName]="i"`. Funciona igual que las habilidades: se agregan y eliminan sin recargar,
queda siempre al menos una y `limpiar()` vuelve a dejar una sola.

**Actividad 4 — Conexión con Spring Boot.** Se resuelve con el `HttpClient` del laboratorio 14 y
en el proyecto final: el formulario de subida de archivos clínicos envía sus datos al backend
Spring Boot, que vuelve a validarlos.

## Pruebas unitarias (`npm test`: 24 en verde)

| Archivo | Qué cubre |
|---------|-----------|
| `registro.validators.spec.ts` | Los tres validadores: vacío delegado a `required`, casos inválidos y válidos |
| `formulario-plantilla.spec.ts` | Sin errores al inicio, errores al enviar vacío, `minlength` y `email`, envío válido y limpiar |
| `formulario-reactivo.spec.ts` | DNI, dominio, edad mínima, error de grupo, `FormArray` de habilidades y de direcciones, envío y limpiar |
| `app.spec.ts` | Cabecera, redirección de `''` y `**`, y carga diferida de `/reactivo` |

## Decisiones propias

- **`limpiar()` reduce los `FormArray` antes de `reset()`.** La guía hace el `reset` primero y
  después quita los controles sobrantes. Al revés, `reset` recibe exactamente un valor por
  control y el formulario queda limpio y `pristine` en un solo paso.
- **Título por ruta.** Con `title` en `app.routes.ts`, la pestaña del navegador indica la vista
  activa.

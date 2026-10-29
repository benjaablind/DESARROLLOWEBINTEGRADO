# Laboratorio 11 — Marco Antonio Gonzales del Valle Vargas

**Fundamentos de Angular, componentes y SASS/SCSS.** Primera interfaz de la Unidad 3: un
*Panel de productos* construido con componentes *standalone*, control de flujo `@if`/`@for` y
estilos SCSS organizados, según la Guía de Laboratorio 11.

Todavía no consume el API Spring Boot, por decisión de la guía (sección 1.2): los datos son
locales y el foco está en los componentes y el estilo.

## Cómo ejecutarlo

```powershell
cd catalogo-angular
npm install
npm start          # http://localhost:4200/
npm test           # 11 pruebas unitarias (Vitest)
npx ng build       # build de produccion en dist/
```

> **Requisito de versión:** Angular 22 exige **Node.js 22.22.3+ o 24.15+**. Con un Node más
> antiguo, el CLI se niega a arrancar. El proyecto se generó y se probó con Node 24.22.2,
> npm 11.19.1 y Angular CLI 22.2.5.

## Qué se construyó

```
catalogo-angular/src/
├── styles.scss                         estilos globales minimos
├── styles/_tokens.scss                 parcial SCSS: colores, medidas y mixins (@use)
└── app/
    ├── app.ts / app.html / app.scss    componente raiz: hero, metricas, buscador y tarjetas
    ├── shared/app-header/              ng g c shared/app-header --style=scss
    ├── shared/app-footer/              ng g c shared/app-footer --style=scss
    └── features/summary-panel/         ng g c features/summary-panel --style=scss
```

| Concepto de la guía | Dónde se usa |
|---------------------|--------------|
| Interpolación `{{ }}` | Título, precios, stock y métricas |
| Event binding `(click)` | Botón *Ocultar stock* / *Mostrar stock* |
| Class binding `[class.out-of-stock]` | Tarjetas de productos agotados |
| `@for` con `track` y `@empty` | Lista de productos y el mensaje sin resultados |
| `@if` / `@else` | Etiqueta *Disponible* / *Agotado* y bloque de stock |
| `signal()` | `mostrarStock`, que alterna la visibilidad del stock |
| `[(ngModel)]` (mini práctica 9) | Buscador: muestra el texto y filtra la lista en tiempo real |
| Componentes standalone importados | `AppHeader`, `SummaryPanel` y `AppFooter` en `imports` de `App` |

### Actividad de consolidación (sección 12)

| Requisito | Cumplimiento |
|-----------|--------------|
| Al menos tres productos con id, nombre, precio y stock | 6 productos |
| `@for` para la colección | Sí, con `track producto.id` y `@empty` |
| `@if` para disponible / agotado | Sí, con `@else` |
| Al menos un event binding | Botón de stock |
| Tres componentes además del raíz | `app-header`, `app-footer` y `summary-panel`, generados con el CLI |
| SCSS: dos variables, un anidamiento y un mixin | Parcial `_tokens.scss` con 11 variables y 3 mixins (`card-shadow`, `pill`, `mobile`); anidamiento con `&` y BEM |
| Nombres con la guía de estilo actual | `app-header.ts`, `summary-panel.html`… (convención 2025, sin `.component`) |
| `ng build` sin errores | Sí |
| **Extensiones 12.2** | Etiqueta *Disponible* / *Agotado*; métricas (productos, unidades y sin stock); diseño responsive con Grid; parcial de tokens con `@use` |

Los componentes no usan `@Input` ni `@Output`, como pide la sección 8: la comunicación entre
componentes es tema de la semana 12. Por eso `summary-panel` tiene contenido estático y las
métricas viven en el componente raíz.

## Evidencias (sección 13)

**E1 — Versiones**

```
node -v      v24.22.2
npm -v       11.19.1
ng version   Angular CLI 22.2.5 · Angular 22.2.4
```

**E2 — Proyecto en el navegador:** [`evidencias/E2_aplicacion_en_navegador.png`](evidencias/E2_aplicacion_en_navegador.png)

**E3 — Estructura de carpetas:** ver el árbol de la sección anterior.

**E4 — `@if` / `@for`:** [`catalogo-angular/src/app/app.html`](catalogo-angular/src/app/app.html) y [`app.ts`](catalogo-angular/src/app/app.ts).

**E5 — Componentes generados con el CLI**

```
ng g c shared/app-header --style=scss
CREATE src/app/shared/app-header/app-header.spec.ts
CREATE src/app/shared/app-header/app-header.ts
CREATE src/app/shared/app-header/app-header.scss
CREATE src/app/shared/app-header/app-header.html
```

Lo mismo con `shared/app-footer` y `features/summary-panel`. Los tres se importan en `App`.

**E6 — SCSS con variables, anidamiento y mixin:** [`src/styles/_tokens.scss`](catalogo-angular/src/styles/_tokens.scss) y [`src/app/app.scss`](catalogo-angular/src/app/app.scss).

**E7 — Resultado responsive:** [`evidencias/E7_responsive_celular.png`](evidencias/E7_responsive_celular.png) (390 px de ancho: una columna, sin desplazamiento horizontal).

**E8 — Build de producción**

```
ng build
Initial chunk files | Names  |  Raw size | Estimated transfer size
main-....js         | main   | 171.37 kB |                48.52 kB
styles-....css      | styles | 152 bytes |               152 bytes
Application bundle generation complete.
```

### Verificación funcional (sección 10)

Comprobado en Chromium con Playwright, además de las pruebas unitarias:

- [x] Abre en `http://localhost:4200/` sin errores de compilación.
- [x] Se ve el título «Catálogo de productos».
- [x] Los productos se muestran con `@for` (6 tarjetas).
- [x] Los productos sin stock se distinguen: borde rojo, etiqueta *Agotado* y opacidad.
- [x] El botón alterna la visibilidad del stock, en ambos sentidos.
- [x] `app-header`, `summary-panel` y `app-footer` aparecen correctamente.
- [x] Los estilos SCSS se aplican; por ejemplo, el borde de un agotado es `$danger`.
- [x] `[(ngModel)]` actualiza el texto en tiempo real y filtra la lista.
- [x] La consola del navegador no muestra errores.

**Pruebas unitarias** (`npm test`): 11 en verde. `app.spec.ts` verifica la interpolación, el
`@for`, los agotados, el botón en ambos sentidos, `ngModel`, `@empty` y las métricas. Las
otras tres son las que genera el CLI para cada componente.

## Notas

- Las respuestas a las preguntas de la sección 14 están en [`RESPUESTAS_REFLEXION.md`](RESPUESTAS_REFLEXION.md).
- `node_modules/` y `dist/` no se versionan: se regeneran con `npm install` y `ng build`.

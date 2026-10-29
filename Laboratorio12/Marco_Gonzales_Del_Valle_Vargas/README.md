# Laboratorio 12 — Marco Antonio Gonzales del Valle Vargas

**Rutas en Angular y comunicación entre componentes.** Una SPA, *DevStore*, que navega entre
vistas sin recargar la página y cuyos componentes intercambian datos de forma explícita y
tipada con `input()` y `output()`, según la Guía de Laboratorio 12.

Usa datos locales simulados (`productos.mock.ts`): el consumo HTTP corresponde a la semana 14
(sección 1.2 de la guía).

## Cómo ejecutarlo

```powershell
cd frontend-semana12
npm install
npm start          # http://localhost:4200/
npm test           # 20 pruebas unitarias (Vitest)
npx ng build       # build de produccion en dist/
```

> **Requisito de versión:** Angular 22 exige **Node.js 22.22.3+ o 24.15+**. El proyecto se
> generó y se probó con Node 24.22.2, npm 11.19.1 y Angular CLI 22.2.5.

## Estructura

```
frontend-semana12/src/app/
├── app.ts / app.html / app.scss     layout: topbar con RouterLink + <router-outlet />
├── app.routes.ts                    '', productos, productos/:id, categorias y '**'
├── app.config.ts                    provideRouter(routes) y locale es-PE
├── models/producto.ts               interfaz Producto
├── data/productos.mock.ts           5 productos locales
├── components/producto-card/        hijo: input() y output()
└── pages/
    ├── inicio/
    ├── productos/                   padre: [producto], (verDetalle), (favorito), (agregarComparacion)
    ├── producto-detalle/            lee :id con ActivatedRoute
    ├── categorias/                  Reto A
    └── no-encontrado/               destino del comodin **
```

Todos los componentes se generaron con `ng g c` (sección 3.4), más `pages/categorias` para el
Reto A.

## Cómo fluye la información

```
URL -> Route -> Component -> RouterOutlet
Productos --[producto] / [destacado]--> ProductoCard          (input)
ProductoCard --(verDetalle) / (favorito) / (agregarComparacion)--> Productos   (output)
Productos --Router.navigate(['/productos', id])--> /productos/2 -> ProductoDetalle lee :id
```

`ProductoCard` solo presenta y emite eventos. No navega ni guarda contadores: el padre decide
qué hacer con cada evento. Por eso la tarjeta es reutilizable, que es el criterio de calidad de
la sección 13.

## Matriz de pruebas manuales (sección 11.2)

Ejecutada en Chromium con Playwright contra `ng serve`. Todas las filas pasan y la consola no
muestra errores.

| Prueba | Acción | Resultado esperado | Resultado |
|--------|--------|--------------------|-----------|
| Inicio | Abrir `http://localhost:4200/` | Inicio dentro del router-outlet | ✔ |
| Navegación | Pulsar *Productos* | `/productos` sin recargar | ✔ 0 descargas de `index.html` |
| Ruta activa | Observar el menú | *Productos* con clase `active` | ✔ |
| Input | Ver las tarjetas | Cada `ProductoCard` muestra su producto | ✔ 5 tarjetas |
| Output favorito | Pulsar *Favorito* dos veces | El padre sube el contador a 2 | ✔ y registra `Favorito recibido desde el hijo: 1` |
| Output + Router | *Ver detalle* del id 2 | Navega a `/productos/2` | ✔ también sin recarga |
| Parámetro | Abrir `/productos/3` | Teclado mecánico | ✔ |
| ID inexistente | Abrir `/productos/999` | Producto no encontrado | ✔ |
| Wildcard | Abrir `/xyz` | Página 404 | ✔ |
| Historial | Atrás / adelante | Responde al historial | ✔ `/productos/2` → `/productos` → `/productos/2` |

**Sin recarga completa (11.3):** además de revisarlo en la pestaña Network, la prueba cuenta las
peticiones de tipo *document*. Al navegar con `routerLink` y con `Router.navigate()` hubo
**cero**: el navegador nunca volvió a descargar `index.html`.

Capturas en [`evidencias/`](evidencias/): inicio, productos con contadores, detalle por
parámetro, producto no encontrado, 404 y categorías.

## Actividad de consolidación (sección 13)

**Reto A — ruta Categorías.** `pages/categorias`, ruta `/categorias` y enlace en el menú.
Muestra las categorías únicas de `PRODUCTOS` (Accesorios, Mobiliario y Tecnología), calculadas
con un `Set` y no escritas a mano, y enlaza cada producto a su detalle.

**Reto B — tarjeta reutilizable.** `destacado = input(false)` agrega la clase `.destacado`
(borde dorado y etiqueta). `Productos` la activa para los productos de más de S/ 1,000: la
Laptop Pro y el Monitor 27 pulgadas.

**Reto C — evento adicional.** `agregarComparacion = output<number>()` emite el id. El padre
guarda los ids sin repetir en un `signal<number[]>` y muestra cuántos hay en comparación.
Pulsar dos veces el mismo producto cuenta una sola vez.

**Reto D — consulta directa.** `/productos/1` y `/productos/999` coinciden con la misma ruta,
`productos/:id`, porque el router compara la **forma** de la URL, no si el recurso existe.
`:id` es un comodín para ese segmento. Por eso las dos llegan a `ProductoDetalle`. La diferencia
la produce el componente: convierte el parámetro a número y lo busca en `PRODUCTOS`. Con 1
encuentra la Laptop Pro y muestra el detalle; con 999 `find()` devuelve `undefined` y el `@else`
muestra «Producto no encontrado». Distinto es `/xyz`: no coincide con ninguna ruta definida y cae
en el comodín `**`, la página 404 de la aplicación.

## Pruebas unitarias (`npm test`: 20 en verde)

| Archivo | Qué cubre |
|---------|-----------|
| `app.spec.ts` | La navegación y el `router-outlet`, más las rutas reales con `RouterTestingHarness`: `/`, `/productos`, `/productos/3`, `/productos/999`, `/categorias` y `/xyz` |
| `producto-card.spec.ts` | `input.required` renderizado, `destacado` por defecto y activado, y los tres `output()` con el id |
| `productos.spec.ts` | `[producto]` a cada tarjeta, destacados > S/ 1,000, favoritos (dos clics → 2), comparación sin repetir y `Router.navigate(['/productos', 2])` |
| `categorias.spec.ts` | Categorías únicas ordenadas y enlace al detalle |
| `inicio` / `no-encontrado` | Creación con el router provisto |

## Decisiones propias

- **Locale `es-PE`.** Con el código de la guía tal cual, `currency:'PEN':'symbol'` muestra
  «PEN3,699.00», porque Angular usa `en-US` por defecto. Registrando `es-PE` en `app.config.ts`,
  el mismo pipe muestra «S/ 3,699.00» sin cambiar ninguna plantilla.
- **Dos productos más en el mock** (ids 4 y 5), para que el Reto A tenga varias categorías. Los
  ids 1 a 3 son los de la guía, así que la matriz 11.2 da los mismos resultados.
- **Título por ruta** (`title` en `app.routes.ts`): la pestaña del navegador refleja la vista
  activa.

Las respuestas a la sección 14 están en [`RESPUESTAS_REFLEXION.md`](RESPUESTAS_REFLEXION.md).

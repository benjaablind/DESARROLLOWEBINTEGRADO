# Reflexión — Laboratorio 11

Marco Antonio Gonzales del Valle Vargas

Respuestas a las preguntas de cierre de la sección 14 de la guía.

## 1. ¿Por qué Angular necesita Node.js si la aplicación finalmente se ejecuta en el navegador?

Porque Node.js no ejecuta la aplicación: ejecuta las **herramientas** que la construyen. El
Angular CLI, el compilador de TypeScript, el compilador de SCSS y el servidor de desarrollo son
programas JavaScript que corren en la computadora, fuera del navegador. Con ellos se transforma
el proyecto (`.ts`, `.html`, `.scss`) en archivos `.js` y `.css` que el navegador sí entiende.
Al terminar `ng build`, la carpeta `dist/` ya no necesita Node: son archivos estáticos que
cualquier servidor web puede entregar. Lo comprobé además con la versión: con Node 24.11 el
CLI de Angular 22 se niega a arrancar, aunque el navegador no tenga nada que ver.

## 2. ¿Qué diferencia existe entre Angular y Angular CLI?

**Angular** es el framework: las librerías (`@angular/core`, `@angular/forms`…) que forman
parte de la aplicación y viajan al navegador, con los componentes, las señales y el
renderizado. **Angular CLI** es la herramienta de línea de comandos que trabaja sobre el
proyecto: lo crea (`ng new`), genera piezas (`ng g c`), lo sirve (`ng serve`), lo prueba y lo
compila (`ng build`). El CLI no llega al navegador; Angular sí.

## 3. ¿Qué ventaja ofrece dividir una interfaz en componentes?

Cada parte tiene una sola responsabilidad y su propio HTML, lógica y estilos. Se puede
entender, probar y modificar sin tocar el resto: cambiar `app-footer` no afecta al panel de
productos. También se reutilizan, porque un componente se usa en cualquier plantilla que lo
importe. Y los estilos quedan encapsulados: la clase `.topbar` de la cabecera no choca con
otra `.topbar` que pudiera existir en otro componente.

## 4. ¿Qué significa que un componente sea standalone?

Que declara él mismo lo que necesita en su arreglo `imports`, sin depender de un `NgModule`
que lo agrupe. Por ejemplo, `App` importa directamente `AppHeader`, `SummaryPanel`, `AppFooter`
y `FormsModule`. Es el modelo por defecto en Angular moderno: un componente no aparece por el
solo hecho de existir en el proyecto, hay que importarlo donde se usa. Si se olvida, el
compilador avisa que no reconoce la etiqueta.

## 5. ¿Qué diferencia existe entre interpolación, property binding y event binding?

- **Interpolación** `{{ titulo }}`: muestra un valor como texto dentro del HTML. Va del
  componente a la vista.
- **Property binding** `[class.out-of-stock]="producto.stock === 0"`: asigna el valor de una
  expresión a una propiedad o atributo de un elemento, no solo texto. También va del componente
  a la vista.
- **Event binding** `(click)="alternarStock()"`: escucha un evento del DOM y ejecuta un método.
  Va de la vista al componente.

`[(ngModel)]` combina los dos sentidos: el input muestra `busqueda` y, al escribir, la
actualiza.

## 6. ¿Cuándo usarías `@if` y cuándo `@for`?

`@if` para decidir **si** se muestra un bloque según una condición: la etiqueta *Disponible* o
*Agotado* según el stock, o el bloque de stock según `mostrarStock()`. `@for` para **repetir**
un bloque por cada elemento de una colección: una tarjeta por producto, con `track producto.id`
para que Angular identifique cada elemento y no rehaga todo el DOM cuando la lista cambia. Se
combinan con frecuencia: dentro del `@for` de productos hay un `@if` por tarjeta.

## 7. ¿Qué ventajas aporta SCSS frente a una hoja CSS extensa?

- **Variables:** el rojo de los agotados está definido una sola vez (`$danger`). Cambiarlo es
  cambiar una línea, no buscar y reemplazar en todo el proyecto.
- **Mixins:** la sombra de tarjeta (`card-shadow`) y las pastillas (`pill`) se escriben una vez
  y se reutilizan.
- **Anidamiento y `&`:** los estilos de `.product-card` y sus variantes quedan agrupados y se
  leen como la estructura del HTML.
- **Módulos con `@use`:** el parcial `_tokens.scss` comparte los colores entre todos los
  componentes sin duplicarlos.

Todo se compila a CSS normal, así que el navegador no necesita nada especial.

## 8. ¿Por qué todavía no consumimos el API Spring Boot en esta semana?

Para aprender una cosa a la vez. Consumir el API suma `HttpClient`, Observables, manejo de
errores, CORS y el token JWT. Si algo fallara, no se sabría si el problema es del componente,
del estilo o de la conexión. Con datos locales, el foco queda en lo que se evalúa esta semana:
componentes, control de flujo y SCSS. Además, la estructura ya queda preparada: cuando llegue la
semana 14, la lista `productos` dejará de estar escrita en el código y vendrá de
`GET /api/productos`, pero el `@for`, las tarjetas y los estilos seguirán siendo los mismos.

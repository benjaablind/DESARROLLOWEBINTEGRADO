# Reflexión — Laboratorio 12

Marco Antonio Gonzales del Valle Vargas

Respuestas a las preguntas de la sección 14 de la guía.

## 1. ¿Cuál es la diferencia entre cambiar de ruta con `routerLink` y usar un enlace `href` tradicional?

Con `href`, el navegador hace una navegación completa: pide de nuevo `index.html` al servidor,
descarta la aplicación en memoria y la vuelve a arrancar desde cero, perdiendo todo el estado
(por ejemplo, el contador de favoritos). Con `routerLink`, Angular intercepta el clic, actualiza
la URL con la History API y solo cambia el componente que se muestra en el `<router-outlet>`.
Lo comprobé contando las peticiones de tipo *document* al navegar: con `routerLink` fueron
cero.

## 2. ¿Qué responsabilidad cumple `RouterOutlet`?

Es el lugar del layout donde el router dibuja el componente de la ruta activa: la «ventana»
que cambia. Todo lo que está fuera de él, como la barra con el menú, permanece fijo entre
vistas. Sin `<router-outlet />` las rutas se resuelven, pero ninguna vista aparece en pantalla.

## 3. ¿Por qué la ruta `**` debe colocarse al final?

Porque el router recorre las rutas en orden y usa la primera que coincide, y `**` coincide con
cualquier URL. Si estuviera primero, atraparía también `/productos` y `/categorias`, y todo
mostraría la página 404. Al final solo recibe lo que ninguna ruta anterior reconoció, como
`/xyz`.

## 4. ¿Qué ventaja ofrece `input.required()` frente a un input opcional?

Convierte el dato en parte obligatoria del contrato del componente. Si un padre usa
`<app-producto-card>` sin `[producto]`, el error aparece al compilar y no en ejecución como un
`undefined` en pantalla. Además, el tipo es `Producto` y no `Producto | undefined`, así que la
plantilla puede usar `producto().nombre` sin comprobaciones defensivas. El opcional sirve
cuando existe un valor por defecto razonable, como `destacado = input(false)`.

## 5. ¿Por qué `output()` reduce el acoplamiento entre el componente hijo y el padre?

Porque el hijo solo anuncia qué pasó, por ejemplo «pidieron el detalle del producto 2», sin
saber quién escucha ni qué se hará. `ProductoCard` no conoce al `Router` ni el contador de
favoritos: emite `verDetalle` y el padre decide navegar. Así la misma tarjeta podría usarse en
otra pantalla donde *Ver detalle* abra un modal en lugar de navegar, sin cambiar una línea del
hijo.

## 6. ¿Cuándo conviene usar un parámetro de ruta en lugar de un `input`?

Cuando el dato identifica **qué se está viendo** y debe vivir en la URL: `/productos/3` se puede
guardar como favorito, compartir, recargar y recorrer con atrás y adelante. Un `input` sirve
para pasar datos entre componentes que ya están en la misma pantalla, como el padre que entrega
cada producto a su tarjeta. En resumen: el parámetro de ruta conecta la URL con una vista, y el
`input` conecta un padre con su hijo.

## 7. ¿Qué parte del flujo de esta práctica será reemplazada por datos del backend en la semana 14?

La fuente de datos: el arreglo `PRODUCTOS` de `productos.mock.ts`. `Productos` pedirá la lista a
`GET /api/productos` y `ProductoDetalle` pedirá `GET /api/productos/{id}` usando el mismo `:id`
de la URL, mediante un servicio con `HttpClient` y el token JWT del back-end de la semana 10. Las
rutas, el `RouterOutlet`, `ProductoCard` con sus `input()` y `output()` y la navegación
programática no cambian. Esa es la ventaja de haber separado la presentación de la obtención de
los datos.

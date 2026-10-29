# Reflexión — Laboratorio 10

Marco Antonio Gonzales del Valle Vargas

Respuestas a las preguntas de la sección 17 de la guía, apoyadas en lo que se observó al
ejecutar la matriz de pruebas contra MySQL.

## 1. ¿Por qué `@Transactional` debe colocarse en una operación de negocio que coordina varios cambios?

Porque la transacción tiene que abarcar la operación completa, y esa operación vive en el
Service. Registrar una salida son dos escrituras: el UPDATE del stock y el INSERT del
movimiento. Si cada una se confirmara por separado, como ocurriría con `@Transactional` en el
Repository, un fallo entre ambas dejaría la mitad hecha. En el Service, Spring abre la
transacción al entrar a `registrarSalida()` y la confirma o la revierte al salir. El Controller
tampoco es el lugar: solo traduce HTTP y no debería saber cuántas tablas toca una regla de
negocio.

## 2. ¿Qué diferencia práctica existe entre una consulta JPQL y una consulta SQL nativa?

JPQL consulta **entidades y atributos de Java** (`FROM Producto p WHERE p.precio BETWEEN ...`), y
Hibernate la traduce al SQL del motor. En la consola se vio cómo `Producto p` se convirtió en
`productos p1_0`. Por eso la consulta no cambia si se renombra la tabla en `@Table`, y Hibernate
la valida contra el modelo al arrancar. El SQL nativo escribe directamente tablas y columnas: da
acceso a funciones propias del motor, pero ata la consulta a ese motor y no la valida contra las
entidades. Conviene usarlo solo cuando JPQL no alcanza.

## 3. ¿Por qué un JWT válido puede terminar en 403?

Porque autenticar y autorizar son dos pasos distintos. El token puede tener la firma correcta,
no estar vencido y venir del emisor esperado: eso prueba **quién** es el usuario, y por eso no
hay 401. Después, Spring compara los roles del token con la regla de la ruta. Si `usuario`, que
solo tiene `ROLE_USER`, pide `GET /api/admin/resumen`, la regla exige `ROLE_ADMIN` y la respuesta
es 403. Lo comprobé en el escenario E09 de la matriz.

## 4. ¿Qué valida Spring Security antes de permitir que una solicitud llegue al Controller?

Para una ruta protegida, en este orden:

1. Que exista `Authorization: Bearer <token>`; si falta, 401.
2. Que la firma HS256 coincida con la clave del servidor; si alguien alteró un carácter, 401
   (escenario E11).
3. Que el token no haya vencido (`exp`) y que el emisor (`iss`) sea `productos-api`; si no, 401.
4. Que los roles del claim `roles`, convertidos en autoridades, cumplan la primera regla de
   `SecurityConfig` que coincide con el método y la ruta; si no, 403.

Recién entonces la petición llega al Controller. Por eso los controladores no tienen ninguna
lógica de autenticación.

## 5. ¿Qué ocurriría si se descontara stock y el insert del movimiento fallara sin una transacción?

El stock quedaría descontado sin el movimiento que lo justifica: la base diría que salieron
productos, pero no habría registro de cuándo ni de cuántos. Es justo el cambio parcial que la
guía pide descartar. Con `@Transactional`, la excepción que sale del método provoca el rollback y
se deshacen las dos operaciones. Lo verifiqué con la salida de 99999 unidades: respondió 409 y
la base quedó exactamente igual, con stock 6 y un solo movimiento.

## 6. ¿Por qué la contraseña no debe aparecer dentro del JWT?

Porque el JWT está **firmado, no cifrado**. Cualquiera que lo tenga puede decodificar el payload
con Base64URL, sin ninguna clave, y leerlo completo. Además viaja en cada petición y suele
quedar en el navegador o en logs. Por eso solo lleva lo necesario para autorizar: el usuario
(`sub`), el emisor, las fechas y los roles. La contraseña solo se usa una vez, en el login, y en
la base se guarda únicamente como hash BCrypt.

## 7. ¿Qué ventaja aporta inyectar Repository/Service por constructor?

Las dependencias quedan declaradas en un solo lugar y pueden ser `final`: el objeto no puede
existir a medias ni cambiar de dependencia después. También facilita las pruebas. En
`AdminControllerSecurityTest`, el `AdminController` recibe un `AdminResumenService` simulado con
`@MockitoBean`, sin levantar la base de datos. Además, si una clase empieza a necesitar
demasiados parámetros en el constructor, es una señal visible de que tiene demasiadas
responsabilidades.

## 8. ¿Qué evidencias permiten afirmar que el back-end está listo para pasar a la integración con Angular?

- Los contratos HTTP son coherentes y predecibles: 200/201 en el éxito, 400 para datos
  inválidos, 401 sin identidad, 403 sin permiso, 404 si el recurso no existe y 409 ante un
  conflicto de negocio, siempre con un cuerpo JSON y su `message`.
- La identidad viaja en un encabezado (`Authorization: Bearer`). Angular solo tendrá que
  adjuntar el token con un interceptor, sin cookies ni sesión en el servidor.
- La seguridad se decide en el servidor: aunque el frontend ocultara mal un botón, el back-end
  responde 403.
- Los datos persisten en MySQL y las operaciones críticas son atómicas.
- Todo está verificado: 56 pruebas automatizadas, la colección de Postman (21 peticiones,
  32 verificaciones, 0 fallas) y un JAR que arranca contra MySQL.

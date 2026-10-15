# Reflexión — Laboratorio 09

Marco Antonio Gonzales del Valle Vargas

La guía de esta semana no trae una lista numerada de preguntas: pide una breve explicación
junto con la prueba JUnit (sección 1.3) y propone una extensión opcional (sección 14). Ambas
están aquí.

## Breve explicación: qué hace el JWT en esta API

**Qué cambió.** Con HTTP Basic, cada petición llevaba usuario y contraseña, y el servidor
volvía a consultar MySQL y a comparar el hash BCrypt en cada una. Con JWT, eso ocurre una sola
vez, en `POST /api/auth/login`. Si las credenciales son válidas, `TokenService` firma un token
con mi identidad (`sub`), el emisor (`iss`), cuándo se emitió (`iat`), cuándo vence (`exp`) y
mis roles. Desde ahí, cada petición solo presenta ese token.

**Por qué el servidor puede confiar en el token sin guardar nada.** Porque está firmado con
HMAC-SHA256 usando una clave que solo conoce el servidor. Al recibirlo, el Resource Server
recalcula la firma: si alguien cambió un solo carácter —por ejemplo, para agregarse
`ROLE_ADMIN`— la firma ya no coincide y la petición se rechaza con 401 antes de llegar al
Controller. Lo comprobé en la fila 09 de la matriz. Además valida que no haya vencido y que el
emisor sea `productos-api`. No hace falta sesión ni consulta a la base: por eso la API es
*stateless*.

**Lo que el token NO es.** Firmado no significa cifrado. Decodifiqué el payload de mi token
con un simple Base64URL, sin ninguna clave, y se lee completo. Por eso nunca debe llevar
contraseñas ni datos sensibles, y por eso HTTPS sigue siendo obligatorio: quien intercepte el
token puede usarlo tal cual hasta que venza.

**Por qué la clave va en una variable de entorno.** Con HS256 la misma clave firma y verifica.
Si estuviera en `application.properties` y se subiera a Git, cualquiera con acceso al
repositorio podría fabricar un token de administrador válido. Por eso la aplicación se niega a
arrancar sin `JWT_SECRET_BASE64`, y exige al menos 32 bytes.

**Qué se conservó de la semana 8.** Los usuarios, los roles, BCrypt, el
`CustomUserDetailsService` y la política completa de autorización (`hasRole`, `hasAnyRole`).
El único detalle nuevo es el `JwtAuthenticationConverter` con prefijo vacío: sin él, Spring
leería el claim `roles` como `SCOPE_ROLE_ADMIN` y `hasRole("ADMIN")` respondería 403 incluso al
administrador.

## Extensión opcional (sección 14)

> *¿Por qué un token con ROLE_ADMIN previamente emitido puede seguir autorizando incluso si
> después retiramos ese rol en MySQL? Propón mitigaciones.*

**Por qué ocurre.** El servidor decide los permisos leyendo el claim `roles` **del propio
token**, no la base de datos. Ese claim se fijó en el momento del login y queda congelado y
firmado. Si después quito `ROLE_ADMIN` a un usuario en `usuarios_roles`, o lo desactivo con
`activo = false`, el token que ya tiene sigue siendo válido —firma correcta, no vencido, emisor
correcto— y sigue diciendo `ROLE_ADMIN`. Nada en la verificación vuelve a consultar MySQL, que
es justamente la ventaja de rendimiento de JWT convertida en riesgo. En mi configuración, esa
ventana dura hasta 30 minutos.

**Mitigaciones, de la más simple a la más completa:**

1. **Expiraciones breves.** Reducir `app.jwt.ttl-minutes` (por ejemplo, a 5–15 minutos) acota la
   ventana en la que un permiso retirado sigue vigente. Normalmente se combina con un *refresh
   token* de mayor duración, que sí se valida contra la base al renovarse: así el rol retirado
   desaparece en la siguiente renovación sin obligar al usuario a hacer login a cada rato.
2. **Revocación por `jti`.** Agregar un identificador único a cada token (claim `jti`) y
   mantener una lista de tokens revocados (en memoria, Redis o una tabla) hasta su `exp`. Al
   retirar un rol o desactivar una cuenta, se revocan los `jti` de ese usuario. Cuesta una
   consulta por petición, pero contra un almacén rápido y solo de tokens revocados.
3. **Versión de sesión por usuario.** Guardar en `usuarios` un número de versión
   (`token_version`) y copiarlo como claim al emitir. Al cambiar roles, desactivar la cuenta o
   cambiar la contraseña, se incrementa la versión; el validador rechaza cualquier token con una
   versión menor. Invalida de golpe todos los tokens de ese usuario sin guardar una lista.
4. **Consultar permisos sensibles en cada solicitud.** Para operaciones críticas —eliminar
   productos, el reporte de administración— no confiar solo en el claim y verificar el rol
   vigente en la base. Puede hacerse en la capa de servicio, como el `@PreAuthorize` sobre
   `eliminar()` que ya tengo desde la semana 8, apoyado en una consulta fresca de roles en vez
   de las autoridades del token.

En un sistema real lo razonable es combinar la 1 con alguna de las otras: expiraciones cortas
para todos y una invalidación explícita (la 2 o la 3) para cuando se retira un permiso o se
bloquea una cuenta.

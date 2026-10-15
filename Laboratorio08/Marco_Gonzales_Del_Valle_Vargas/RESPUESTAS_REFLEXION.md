# Preguntas de reflexión — Laboratorio 08

Marco Antonio Gonzales del Valle Vargas

**1. ¿Cuál es la diferencia entre autenticación y autorización?**
La autenticación responde *¿quién eres?*: comprueba la identidad, en este laboratorio
validando usuario y contraseña contra MySQL. La autorización responde *¿qué puedes hacer?*:
con la identidad ya establecida, decide si ese usuario puede ejecutar la operación pedida.
Siempre ocurren en ese orden, porque no se puede decidir qué permisos tiene alguien sin saber
quién es. En la API, `usuario` y `admin` se autentican igual de bien, pero solo `admin` está
autorizado a hacer `POST /api/productos`.

**2. ¿Por qué hasRole("ADMIN") busca una autoridad ROLE_ADMIN?**
Porque Spring Security modela todos los permisos como `GrantedAuthority`, que son simples
cadenas, y usa la convención del prefijo `ROLE_` para distinguir los roles de otros permisos
más finos (como `producto:eliminar` o los `SCOPE_` de OAuth2). `hasRole("ADMIN")` es un atajo
que antepone ese prefijo y compara contra `ROLE_ADMIN`; la forma explícita equivalente es
`hasAuthority("ROLE_ADMIN")`. Por eso guardé los roles en la tabla ya como `ROLE_USER` y
`ROLE_ADMIN`: si guardara solo `ADMIN`, la regla buscaría `ROLE_ADMIN`, no lo encontraría y el
administrador recibiría 403 sin un error que explique por qué.

**3. ¿Por qué una contraseña debe almacenarse con un PasswordEncoder?**
Porque si la base de datos se filtra —por un respaldo expuesto, una inyección SQL o un acceso
indebido— las contraseñas en texto plano quedan inmediatamente utilizables, y como la gente
las reutiliza, el daño se extiende a otros sistemas. BCrypt guarda un hash de una sola vía:
la aplicación puede verificar si una contraseña coincide sin poder recuperar la original. Además
agrega un *salt* aleatorio por contraseña, de modo que dos usuarios con la misma clave tienen
hashes distintos y no sirven las tablas precalculadas, y tiene un factor de trabajo que lo hace
deliberadamente lento, lo que encarece los ataques de fuerza bruta. Cifrar de forma reversible
no resolvería el problema: quien robe la base probablemente también pueda robar la clave.

**4. ¿Qué diferencia existe entre un 401 y un 403?**
**401 Unauthorized** significa que no hay una autenticación válida: no se enviaron credenciales
o son incorrectas. El servidor no sabe quién eres, y reintentar con credenciales correctas puede
resolverlo. **403 Forbidden** significa que el servidor sí sabe quién eres, pero esa identidad
no tiene permiso para el recurso: reintentar con las mismas credenciales nunca va a funcionar.
En la matriz de pruebas, `GET /api/productos` sin credenciales da 401, y `POST /api/productos`
con `usuario` da 403. (El nombre "Unauthorized" del 401 es históricamente confuso: en la
práctica significa *no autenticado*.)

**5. ¿Qué responsabilidad cumple UserDetailsService?**
Es el contrato con el que Spring Security pide los datos de un usuario a partir de su nombre,
sin saber dónde ni cómo están guardados. Mi `CustomUserDetailsService` busca el `Usuario` en
MySQL con `findByUsername`, convierte cada `Rol` en una autoridad y devuelve un `UserDetails`
con el hash de la contraseña, las autoridades y si la cuenta está habilitada. Lo que **no**
hace es comparar contraseñas: eso lo hace el `DaoAuthenticationProvider` usando el
`PasswordEncoder`. Esa separación permite cambiar el origen de los usuarios (otra tabla, LDAP,
un servicio externo) sin tocar nada del resto de la configuración de seguridad.

**6. ¿Por qué las reglas más específicas deben declararse antes que anyRequest()?**
Porque Spring Security evalúa las reglas en el orden declarado y aplica **la primera que
coincide**; las siguientes ni se consultan. `anyRequest()` coincide con todo, así que cualquier
regla que venga después es inalcanzable. De hecho, Spring Security 7 se niega a arrancar si se
declara algo después de `anyRequest()`. El mismo razonamiento aplica entre reglas específicas:
si pusiera `GET /api/**` con `permitAll()` antes que `GET /api/productos/**`, los productos
quedarían públicos sin ningún aviso. El orden correcto es de lo más particular a lo más general.

**7. ¿Por qué en una API REST stateless se puede tomar una decisión diferente sobre CSRF que en una aplicación web con sesión?**
CSRF explota que el navegador adjunta **automáticamente** las cookies de sesión a cualquier
petición hacia un dominio: un sitio malicioso puede hacer que el navegador de la víctima envíe
un `POST` a la API y la cookie viajará sola, autenticando una acción que el usuario nunca pidió.
En esta API no hay sesión ni cookie de autenticación (`SessionCreationPolicy.STATELESS`): las
credenciales van en el encabezado `Authorization`, que el navegador no agrega por su cuenta y
que un sitio de terceros no puede fijar. Sin ese envío automático, el ataque no tiene con qué
autenticarse, y por eso es razonable desactivar CSRF. Pero la decisión depende del mecanismo, no
de que sea "una API REST": si más adelante el frontend guardara el token en una cookie, habría
que volver a activar la protección.

**8. ¿Qué parte de esta solución podremos reutilizar cuando incorporemos JWT en la Semana 9?**
Casi todo lo que no tiene que ver con *cómo viajan* las credenciales. Se reutilizan las
entidades `Usuario` y `Rol`, sus repositorios, las tablas `usuarios`, `roles` y
`usuarios_roles`, el `CustomUserDetailsService`, el `PasswordEncoder` BCrypt,
`DatosSeguridadIniciales`, el `PublicoController` y, sobre todo, la política de autorización por
rol completa (`hasRole`, `hasAnyRole`, el orden de las reglas). Lo que cambia es el transporte:
HTTP Basic se reemplaza por un login que valida una sola vez con ese mismo `UserDetailsService`
y BCrypt, emite un JWT con los roles, y las peticiones siguientes presentan el token en vez de
la contraseña.

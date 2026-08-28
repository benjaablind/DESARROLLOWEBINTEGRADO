# Preguntas de reflexión - Laboratorio 03 (TDD en Spring Boot)

1. **¿Por qué TDD no es lo mismo que escribir pruebas después de terminar el código?**
   Porque en TDD la prueba define el comportamiento esperado antes de que exista el código, guiando el diseño. Escribir pruebas después solo confirma lo que ya se implementó, pero no influye en cómo se diseñó la solución.

2. **¿Qué representa la fase RED?**
   Representa el momento en que se declara una regla de negocio como una prueba que aún falla (o no compila), porque la funcionalidad todavía no existe. Confirma que la prueba realmente valida algo nuevo.

3. **¿Por qué no debe saltarse la fase REFACTOR?**
   Porque sin refactorizar el código tiende a acumular duplicación y mala estructura. Refactorizar con las pruebas en verde permite mejorar el diseño con la garantía de que el comportamiento no se rompe.

4. **¿Qué diferencia hay entre probar ProductoService y ProductoController?**
   ProductoServiceTest valida la lógica de negocio de forma aislada (reglas de validación, cálculos), sin levantar el contexto de Spring. ProductoControllerTest valida la capa web: códigos HTTP, formato JSON y que el controlador delegue correctamente en el servicio.

5. **¿Para qué sirve MockMvc?**
   Permite simular peticiones HTTP (GET, POST, PUT, DELETE) contra los controladores sin necesidad de levantar un servidor real, verificando el código de estado y el cuerpo de la respuesta.

6. **¿Por qué en @WebMvcTest se usa un mock del servicio?**
   Porque @WebMvcTest solo carga la capa web (controladores), no el contexto completo de Spring. Al mockear el servicio con @MockitoBean se aísla la prueba del controlador de la lógica de negocio real.

7. **¿Qué error se genera cuando Angular o Postman envía un JSON con tipos incorrectos?**
   Normalmente se produce un error 400 Bad Request (HttpMessageNotReadableException), porque Jackson no puede convertir el JSON recibido al tipo de dato esperado en la clase del modelo.

8. **¿Cómo ayuda TDD a mejorar el diseño de una API REST?**
   Obliga a pensar primero en el contrato (entradas, salidas, reglas de negocio y casos de error) antes de programar. Esto produce clases con responsabilidades más claras, menos código innecesario y una API cuyo comportamiento está respaldado por pruebas automatizadas.

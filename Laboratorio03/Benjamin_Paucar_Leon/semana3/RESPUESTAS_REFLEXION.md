# Preguntas de reflexion - Laboratorio 03 (TDD en Spring Boot)

1. **Por que TDD no es lo mismo que escribir pruebas despues de terminar el codigo?**
   Porque en TDD la prueba define el comportamiento esperado antes de que exista el codigo, guiando el diseno. Escribir pruebas despues solo confirma lo ya implementado, pero no influye en como se diseno la solucion.

2. **Que representa la fase RED?**
   Representa el momento en que se declara una regla de negocio como una prueba que aun falla (o no compila), porque la funcionalidad todavia no existe. Confirma que la prueba realmente valida algo nuevo.

3. **Por que no debe saltarse la fase REFACTOR?**
   Porque sin refactorizar el codigo tiende a acumular duplicacion y mala estructura. Refactorizar con las pruebas en verde permite mejorar el diseno con la garantia de que el comportamiento no se rompe.

4. **Que diferencia hay entre probar ProductoService y ProductoController?**
   ProductoServiceTest valida la logica de negocio de forma aislada (reglas de validacion, calculos), sin levantar el contexto de Spring. ProductoControllerTest valida la capa web: codigos HTTP, formato JSON y que el controlador delegue correctamente en el servicio.

5. **Para que sirve MockMvc?**
   Permite simular peticiones HTTP (GET, POST, PUT, DELETE) contra los controladores sin necesidad de levantar un servidor real, verificando el codigo de estado y el cuerpo de la respuesta.

6. **Por que en @WebMvcTest se usa un mock del servicio?**
   Porque @WebMvcTest solo carga la capa web (controladores), no el contexto completo de Spring. Al mockear el servicio con @MockitoBean se aisla la prueba del controlador de la logica de negocio real.

7. **Que error se genera cuando Angular o Postman envia un JSON con tipos incorrectos?**
   Normalmente se produce un error 400 Bad Request (HttpMessageNotReadableException), porque Jackson no puede convertir el JSON recibido al tipo de dato esperado en la clase del modelo.

8. **Como ayuda TDD a mejorar el diseno de una API REST?**
   Obliga a pensar primero en el contrato (entradas, salidas, reglas de negocio y casos de error) antes de programar. Esto produce clases con responsabilidades mas claras, menos codigo innecesario y una API cuyo comportamiento esta respaldado por pruebas automatizadas.

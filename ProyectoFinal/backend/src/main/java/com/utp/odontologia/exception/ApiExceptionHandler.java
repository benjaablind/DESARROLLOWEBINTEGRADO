package com.utp.odontologia.exception;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.utp.odontologia.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Manejo centralizado de errores para toda la API.
 * Evita repetir try-catch en cada controlador y garantiza una respuesta JSON uniforme.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 404: el recurso solicitado no existe. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 400: se incumple una regla de negocio. */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 400: fallo una validacion declarada en el DTO. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validaciones(MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /** 400: fallo una validacion sobre parametros sueltos del controlador. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> violaciones(ConstraintViolationException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 400: el cuerpo no es JSON valido o un campo tiene el tipo incorrecto. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON valido o contiene tipos incorrectos",
                request);
    }

    /** 400: un parametro de ruta o de consulta tiene un valor invalido. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue(),
                request);
    }

    /** 400: falta un parametro obligatorio de consulta. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> parametroFaltante(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parametro obligatorio '" + ex.getParameterName() + "'", request);
    }

    /** 400: el archivo subido supera el tamano permitido. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> archivoMuyGrande(MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El archivo supera el tamano maximo permitido", request);
    }

    /**
     * 409: la base de datos rechazo la operacion por integridad referencial o
     * por un valor unico repetido. El caso tipico es eliminar un paciente que
     * todavia tiene citas, tratamientos o pagos: con la persistencia en memoria
     * se borraba y quedaban registros huerfanos; las claves foraneas lo impiden.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> conflictoDeIntegridad(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT,
                "La operacion no se puede completar porque el registro esta relacionado con otros datos",
                request);
    }

    /**
     * 403 en el login: la contrasena es correcta pero el usuario fue dado de
     * baja. Solo se informa despues de validar la clave (ver SecurityConfig).
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> usuarioInactivo(DisabledException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "El usuario esta inactivo", request);
    }

    /**
     * 401 en el login: usuario inexistente o contrasena incorrecta. El mismo
     * mensaje para ambos casos, para no revelar que nombres de usuario existen.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> credencialesInvalidas(AuthenticationException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Usuario o contrasena incorrectos", request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(estado.value(), mensaje, request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }
}

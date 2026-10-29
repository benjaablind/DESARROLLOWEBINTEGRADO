package com.utp.productosapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> reglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Semana 10: stock insuficiente es un conflicto con el estado actual del
     * producto (409), no un dato mal escrito (400). Spring elige este handler
     * por ser el de la excepcion mas especifica.
     */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> stockInsuficiente(StockInsuficienteException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Semana 9: @Valid sobre LoginRequest. Un login con username o password
     * vacio se rechaza con 400 antes de intentar autenticar.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(MethodArgumentNotValidException ex) {
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField())
                .distinct()
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("solicitud");
        return respuesta(HttpStatus.BAD_REQUEST, "Campos obligatorios: " + campos);
    }

    /**
     * Semana 10 (errores frecuentes, "400 en query de rango"): parametro
     * ausente, como /rango?min=100 sin max. Se responde aqui con 400 y un
     * mensaje claro; si se dejara a Spring, el reenvio interno a /error
     * pasaria por la cadena de seguridad sin token y terminaria en 401.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> parametroFaltante(MissingServletRequestParameterException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Falta el parametro obligatorio: " + ex.getParameterName());
    }

    /** Parametro con tipo incorrecto, como /rango?min=abc. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Valor invalido para el parametro: " + ex.getName());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> errorSimulado(IllegalStateException ex) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", mensaje));
    }
}

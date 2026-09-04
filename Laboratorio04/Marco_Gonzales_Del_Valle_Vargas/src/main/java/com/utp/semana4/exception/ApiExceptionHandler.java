package com.utp.semana4.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.utp.semana4.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Manejo centralizado de errores. Evita repetir try-catch en cada endpoint
 * y garantiza que el cliente siempre reciba un JSON con la misma estructura.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 404: el producto solicitado no existe. */
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarProductoNoEncontrado(
            ProductoNoEncontradoException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** 400: fallo una validacion declarada en el DTO (@NotBlank, @Positive, etc.). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /** 400: regla de negocio (ejercicio 2) — el stock no puede quedar negativo. */
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> manejarStockInsuficiente(
            StockInsuficienteException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** 400: el body no es JSON valido o un campo tiene un tipo incorrecto (ej. "precio": "abc"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no es un JSON valido o contiene tipos incorrectos",
                request);
    }

    /** 400: un parametro de ruta o query tiene un tipo incorrecto (ej. /api/productos/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoDeParametro(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue(),
                request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje,
            HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(estado.value(), mensaje, request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }
}

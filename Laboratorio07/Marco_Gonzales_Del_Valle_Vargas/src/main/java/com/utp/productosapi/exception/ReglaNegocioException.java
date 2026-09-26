package com.utp.productosapi.exception;

/**
 * Regla de negocio incumplida: stock insuficiente, cantidad no positiva,
 * rango de precios invertido, etc.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

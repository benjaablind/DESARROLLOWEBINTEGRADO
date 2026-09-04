package com.utp.semana4.exception;

/**
 * Regla de negocio del ejercicio complementario 2: el stock nunca puede
 * quedar negativo. El manejador global la traduce a 400 Bad Request.
 */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(Long id, int stockActual, int cantidad) {
        super("Stock insuficiente para el producto con id " + id
                + ": stock actual " + stockActual + ", se intento disminuir " + cantidad);
    }
}

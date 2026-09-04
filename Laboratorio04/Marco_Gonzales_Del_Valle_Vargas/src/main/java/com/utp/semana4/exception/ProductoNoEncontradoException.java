package com.utp.semana4.exception;

/**
 * Se lanza cuando el cliente consulta, actualiza o elimina un producto que no existe.
 * El manejador global la traduce a 404 Not Found.
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(Long id) {
        super("No existe un producto con id: " + id);
    }
}

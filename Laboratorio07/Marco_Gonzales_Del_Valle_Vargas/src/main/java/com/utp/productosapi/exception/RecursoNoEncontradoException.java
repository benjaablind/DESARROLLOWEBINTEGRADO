package com.utp.productosapi.exception;

/**
 * Extiende RuntimeException: al propagarse, Spring revierte la transaccion.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

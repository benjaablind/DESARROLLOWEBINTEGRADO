package com.utp.odontologia.exception;

/**
 * Se lanza cuando se solicita un recurso que no existe. Se traduce a 404 Not Found.
 * La usan todos los modulos para mantener el mismo comportamiento.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super("No existe " + recurso + " con id: " + id);
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

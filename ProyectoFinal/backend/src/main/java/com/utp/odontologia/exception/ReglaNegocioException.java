package com.utp.odontologia.exception;

/**
 * Se lanza cuando la solicitud es valida en formato pero incumple una regla de
 * negocio (por ejemplo, un pago mayor al saldo pendiente o un horario ocupado).
 * Se traduce a 400 Bad Request.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

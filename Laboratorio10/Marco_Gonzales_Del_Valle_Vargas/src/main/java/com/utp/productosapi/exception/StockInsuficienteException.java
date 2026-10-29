package com.utp.productosapi.exception;

/**
 * Semana 10: la salida pide mas unidades de las que hay. Se responde 409
 * Conflict (guia, seccion 12.3): la peticion es valida, pero choca con el
 * estado actual del recurso. Sigue siendo una ReglaNegocioException, asi que
 * todo el codigo que ya la trataba como regla de negocio sigue funcionando.
 */
public class StockInsuficienteException extends ReglaNegocioException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}

package com.utp.odontologia.dto;

/**
 * Textos compartidos por las respuestas de todos los modulos.
 *
 * Vive en la base del proyecto y no dentro de un modulo concreto, para que
 * ninguno dependa de otro solo por reutilizar una cadena.
 */
public final class Textos {

    /** Se usa cuando un dato referenciado por id ya no existe. */
    public static final String SIN_DATO = "(no disponible)";

    private Textos() {
    }
}

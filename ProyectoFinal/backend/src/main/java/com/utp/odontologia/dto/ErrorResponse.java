package com.utp.odontologia.dto;

import java.time.LocalDateTime;

/**
 * Estructura JSON uniforme para todos los errores de la API.
 * Acordada por el equipo para que el frontend maneje los errores de una sola forma.
 */
public class ErrorResponse {

    private final int estado;
    private final String mensaje;
    private final String ruta;
    private final LocalDateTime fechaHora;

    public ErrorResponse(int estado, String mensaje, String ruta) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.fechaHora = LocalDateTime.now();
    }

    public int getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}

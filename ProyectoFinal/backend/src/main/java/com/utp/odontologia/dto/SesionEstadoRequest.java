package com.utp.odontologia.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo para marcar una sesion como realizada o pendiente (Integrante 5).
 * Se usa en PATCH /api/tratamientos/{id}/sesiones/{sesionId}.
 */
public record SesionEstadoRequest(

        @NotNull(message = "Debe indicar si la sesion fue realizada")
        Boolean realizada,

        String observaciones) {
}

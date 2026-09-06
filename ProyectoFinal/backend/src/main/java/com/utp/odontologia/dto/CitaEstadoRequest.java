package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoCita;

import jakarta.validation.constraints.NotNull;

/**
 * Cambio manual del estado de una cita (Integrante 3).
 * Las observaciones son opcionales y sirven para dejar constancia del motivo.
 */
public record CitaEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoCita estado,

        String observaciones) {
}

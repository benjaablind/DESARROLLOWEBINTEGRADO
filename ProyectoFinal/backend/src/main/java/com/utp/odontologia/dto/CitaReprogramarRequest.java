package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

/**
 * Datos para reprogramar una cita (Integrante 3).
 * La cita original queda como REPROGRAMADA y se crea una cita nueva.
 */
public record CitaReprogramarRequest(

        @NotNull(message = "La nueva fecha y hora son obligatorias")
        @Future(message = "La nueva fecha de la cita debe ser futura")
        LocalDateTime nuevaFechaHora,

        String motivo) {
}

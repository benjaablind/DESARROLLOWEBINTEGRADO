package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que envia el cliente para registrar o actualizar una cita (Integrante 3).
 * Se valida con @Valid en el controlador; ApiExceptionHandler traduce los errores a 400.
 */
public record CitaRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El odontologo es obligatorio")
        Long odontologoId,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La fecha de la cita debe ser futura")
        LocalDateTime fechaHora,

        // Opcional: si llega null el servicio aplica la duracion por defecto de 30 minutos.
        @Min(value = 10, message = "La duracion minima es de 10 minutos")
        @Max(value = 240, message = "La duracion maxima es de 240 minutos")
        Integer duracionMinutos,

        @NotBlank(message = "El motivo es obligatorio")
        String motivo,

        String observaciones) {

    /** Duracion efectiva: la enviada por el cliente o 30 minutos por defecto. */
    public int duracionEfectiva() {
        return duracionMinutos == null ? 30 : duracionMinutos;
    }
}

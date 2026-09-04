package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoTratamiento;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo del cambio de estado de un tratamiento (Integrante 5).
 * Se usa en PATCH /api/tratamientos/{id}/estado.
 */
public record TratamientoEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoTratamiento estado) {
}

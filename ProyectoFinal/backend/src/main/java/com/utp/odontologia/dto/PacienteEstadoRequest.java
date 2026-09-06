package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoPaciente;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo del cambio de estado de un paciente (Integrante 2).
 * Se usa en PATCH /api/pacientes/{id}/estado.
 */
public record PacienteEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        EstadoPaciente estado) {
}

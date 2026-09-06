package com.utp.odontologia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar una consulta de historia clinica (Integrante 4).
 *
 * No existe un DTO de actualizacion: una consulta ya registrada nunca se
 * sobrescribe, solo puede anularse.
 */
public record HistoriaClinicaRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El odontologo es obligatorio")
        Long odontologoId,

        /** Cita de la que proviene la consulta. Es opcional. */
        Long citaId,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        String motivoConsulta,

        String anamnesis,

        String examenClinico,

        @NotBlank(message = "El diagnostico es obligatorio")
        String diagnostico,

        String procedimiento,

        String tratamiento,

        String medicamentos,

        String observaciones) {
}

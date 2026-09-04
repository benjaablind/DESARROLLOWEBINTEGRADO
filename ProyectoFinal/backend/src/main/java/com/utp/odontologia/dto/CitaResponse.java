package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;

/**
 * Vista de una cita para el cliente (Integrante 3).
 * Incluye los nombres del paciente y del odontologo ya resueltos para que el
 * frontend no tenga que hacer consultas adicionales.
 */
public record CitaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long odontologoId,
        String odontologoNombre,
        LocalDateTime fechaHora,
        LocalDateTime fechaHoraFin,
        int duracionMinutos,
        String motivo,
        EstadoCita estado,
        String observaciones,
        Long citaOriginalId,
        LocalDateTime fechaRegistro) {

    /** Texto que se usa cuando el paciente o el odontologo ya no existe. */
    public static final String SIN_DATO = "(no disponible)";

    /** Convierte el modelo interno en la respuesta que ve el cliente. */
    public static CitaResponse desde(Cita cita, String pacienteNombre, String odontologoNombre) {
        return new CitaResponse(
                cita.getId(),
                cita.getPacienteId(),
                pacienteNombre == null ? SIN_DATO : pacienteNombre,
                cita.getOdontologoId(),
                odontologoNombre == null ? SIN_DATO : odontologoNombre,
                cita.getFechaHora(),
                cita.getFechaHoraFin(),
                cita.getDuracionMinutos(),
                cita.getMotivo(),
                cita.getEstado(),
                cita.getObservaciones(),
                cita.getCitaOriginalId(),
                cita.getFechaRegistro());
    }
}

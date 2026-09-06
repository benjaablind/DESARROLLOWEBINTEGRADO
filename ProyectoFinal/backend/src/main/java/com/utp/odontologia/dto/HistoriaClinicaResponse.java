package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.HistoriaClinica;

/**
 * Vista de salida de una consulta de historia clinica (Integrante 4).
 *
 * Incorpora los nombres del paciente y del odontologo para que el frontend no
 * tenga que pedirlos en llamadas adicionales.
 */
public record HistoriaClinicaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long odontologoId,
        String odontologoNombre,
        Long citaId,
        LocalDateTime fecha,
        String motivoConsulta,
        String anamnesis,
        String examenClinico,
        String diagnostico,
        String procedimiento,
        String tratamiento,
        String medicamentos,
        String observaciones,
        boolean anulada,
        String motivoAnulacion) {

    /** Texto que se muestra cuando el paciente o el odontologo ya no existen. */
    public static final String NOMBRE_NO_DISPONIBLE = "(no disponible)";

    /**
     * Convierte la entidad del dominio en su vista de salida.
     *
     * @param historia          consulta registrada
     * @param pacienteNombre    nombre completo del paciente, o null
     * @param odontologoNombre  nombre completo del odontologo, o null
     */
    public static HistoriaClinicaResponse desde(HistoriaClinica historia, String pacienteNombre,
            String odontologoNombre) {
        return new HistoriaClinicaResponse(
                historia.getId(),
                historia.getPacienteId(),
                pacienteNombre == null ? NOMBRE_NO_DISPONIBLE : pacienteNombre,
                historia.getOdontologoId(),
                odontologoNombre == null ? NOMBRE_NO_DISPONIBLE : odontologoNombre,
                historia.getCitaId(),
                historia.getFecha(),
                historia.getMotivoConsulta(),
                historia.getAnamnesis(),
                historia.getExamenClinico(),
                historia.getDiagnostico(),
                historia.getProcedimiento(),
                historia.getTratamiento(),
                historia.getMedicamentos(),
                historia.getObservaciones(),
                historia.isAnulada(),
                historia.getMotivoAnulacion());
    }
}

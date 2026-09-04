package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;

/**
 * Vista reducida de un paciente para los listados (Integrante 2).
 * Evita enviar antecedentes y direccion en respuestas que pueden ser largas.
 */
public record PacienteResumenResponse(
        Long id,
        String dni,
        String nombreCompleto,
        Integer edad,
        String telefono,
        EstadoPaciente estado) {

    /** Convierte la entidad del dominio en su resumen de salida. */
    public static PacienteResumenResponse desde(Paciente paciente) {
        return new PacienteResumenResponse(
                paciente.getId(),
                paciente.getDni(),
                paciente.getNombreCompleto(),
                paciente.getEdad(),
                paciente.getTelefono(),
                paciente.getEstado());
    }
}

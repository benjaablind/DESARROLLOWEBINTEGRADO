package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;

/** Fila de paciente dentro de los reportes (Integrante 6). */
public record ReportePacienteItem(
        Long id,
        String dni,
        String nombreCompleto,
        Integer edad,
        String telefono,
        EstadoPaciente estado,
        LocalDateTime fechaRegistro) {

    /** Arma la fila a partir de la entidad del dominio. */
    public static ReportePacienteItem desde(Paciente paciente) {
        return new ReportePacienteItem(
                paciente.getId(),
                paciente.getDni(),
                paciente.getNombreCompleto(),
                paciente.getEdad(),
                paciente.getTelefono(),
                paciente.getEstado(),
                paciente.getFechaRegistro());
    }
}

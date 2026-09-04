package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.Paciente;

/** Fila compacta de paciente usada en el tablero de control (Integrante 3). */
public record DashboardPacienteResumen(
        Long id,
        String nombreCompleto,
        String telefono,
        LocalDateTime fechaRegistro) {

    public static DashboardPacienteResumen desde(Paciente paciente) {
        return new DashboardPacienteResumen(
                paciente.getId(),
                paciente.getNombreCompleto(),
                paciente.getTelefono(),
                paciente.getFechaRegistro());
    }
}

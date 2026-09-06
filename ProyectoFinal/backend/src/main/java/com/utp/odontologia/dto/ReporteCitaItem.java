package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.EstadoCita;

/** Fila de cita dentro de los reportes (Integrante 6). */
public record ReporteCitaItem(
        Long id,
        String pacienteNombre,
        String odontologoNombre,
        LocalDateTime fechaHora,
        String motivo,
        EstadoCita estado) {
}

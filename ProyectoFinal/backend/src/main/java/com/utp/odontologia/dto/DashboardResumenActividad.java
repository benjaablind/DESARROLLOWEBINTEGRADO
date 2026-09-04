package com.utp.odontologia.dto;

/** Conteos del mes en curso que se muestran en el tablero (Integrante 3). */
public record DashboardResumenActividad(
        long citasAtendidasMes,
        long citasCanceladasMes,
        long citasNoAsistioMes,
        long tratamientosCompletadosMes,
        long nuevosPacientesMes) {
}

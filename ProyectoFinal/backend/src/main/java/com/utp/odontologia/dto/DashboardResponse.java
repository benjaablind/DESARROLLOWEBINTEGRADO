package com.utp.odontologia.dto;

import java.util.List;

/**
 * Respuesta unica del tablero de control (Integrante 3).
 * Reune los indicadores del sistema en una sola llamada para que el frontend
 * pinte la pantalla de inicio sin encadenar varias peticiones.
 */
public record DashboardResponse(
        long pacientesRegistrados,
        long citasHoy,
        long citasPendientes,
        long tratamientosActivos,
        double pagosPendientes,
        double ingresosDelMes,
        List<CitaResponse> proximasCitas,
        List<DashboardPacienteResumen> pacientesRecientes,
        List<DashboardTratamientoResumen> tratamientosActivosDetalle,
        List<String> alertas,
        DashboardResumenActividad resumenActividad) {
}

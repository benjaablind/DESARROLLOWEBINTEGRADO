package com.utp.odontologia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de tratamientos de un periodo (Integrante 6).
 * Separa los realizados (completados dentro del rango) de los pendientes
 * (los que siguen activos). Los montos son la suma de los precios acordados.
 */
public record ReporteTratamientosResponse(
        LocalDate desde,
        LocalDate hasta,
        long totalRealizados,
        long totalPendientes,
        BigDecimal montoRealizados,
        BigDecimal montoPendientes,
        List<ReporteTratamientoItem> tratamientos) {
}

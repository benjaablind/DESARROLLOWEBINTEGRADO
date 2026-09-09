package com.utp.odontologia.dto;

import java.util.List;

/**
 * Reporte de deuda por cobrar (Integrante 6).
 * Solo considera tratamientos activos cuyo saldo sigue siendo mayor a cero.
 */
public record ReportePagosPendientesResponse(
        double totalPendiente,
        long cantidadTratamientos,
        List<ReporteTratamientoItem> items) {
}

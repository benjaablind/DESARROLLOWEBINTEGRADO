package com.utp.odontologia.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Reporte de pacientes registrados en un periodo (Integrante 6).
 * Incluye los totales por estado para no recalcularlos en el frontend.
 */
public record ReportePacientesResponse(
        LocalDate desde,
        LocalDate hasta,
        long total,
        long activos,
        long inactivos,
        List<ReportePacienteItem> pacientes) {
}

package com.utp.odontologia.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reporte de citas de un periodo (Integrante 6).
 * El mapa porEstado trae siempre todos los estados, incluso los que quedan en
 * cero, para que los graficos del frontend tengan un eje estable.
 */
public record ReporteCitasResponse(
        LocalDate desde,
        LocalDate hasta,
        long total,
        Map<String, Long> porEstado,
        List<ReporteCitaItem> citas) {
}

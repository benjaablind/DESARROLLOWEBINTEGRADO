package com.utp.odontologia.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * Reporte economico de ingresos (Integrante 6).
 * Agrupa los pagos cobrados por metodo y por mes (clave con formato yyyy-MM).
 */
public record ReporteIngresosResponse(
        LocalDate desde,
        LocalDate hasta,
        double totalIngresos,
        long cantidadPagos,
        Map<String, Double> porMetodo,
        Map<String, Double> porMes) {
}

package com.utp.odontologia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.utp.odontologia.model.EstadoTratamiento;

/**
 * Fila de tratamiento dentro de los reportes (Integrante 6).
 * Trae el consolidado economico ya calculado: lo pagado y el saldo por cobrar.
 */
public record ReporteTratamientoItem(
        Long id,
        String pacienteNombre,
        String nombre,
        EstadoTratamiento estado,
        BigDecimal precio,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}

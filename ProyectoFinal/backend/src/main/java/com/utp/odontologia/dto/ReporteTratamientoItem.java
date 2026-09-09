package com.utp.odontologia.dto;

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
        double precio,
        double totalPagado,
        double saldoPendiente,
        LocalDate fechaInicio,
        LocalDate fechaFin) {
}

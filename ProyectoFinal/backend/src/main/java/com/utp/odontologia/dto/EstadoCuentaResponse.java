package com.utp.odontologia.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumen economico de un paciente (Integrante 5).
 * Suma todos sus tratamientos vigentes (no cancelados) con lo pagado y lo que debe.
 */
public record EstadoCuentaResponse(
        Long pacienteId,
        String pacienteNombre,
        BigDecimal totalTratamientos,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente,
        List<TratamientoResponse> tratamientos) {
}

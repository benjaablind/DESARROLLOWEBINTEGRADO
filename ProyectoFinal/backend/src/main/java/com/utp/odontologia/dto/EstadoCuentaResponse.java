package com.utp.odontologia.dto;

import java.util.List;

/**
 * Resumen economico de un paciente (Integrante 5).
 * Suma todos sus tratamientos vigentes (no cancelados) con lo pagado y lo que debe.
 */
public record EstadoCuentaResponse(
        Long pacienteId,
        String pacienteNombre,
        double totalTratamientos,
        double totalPagado,
        double saldoPendiente,
        List<TratamientoResponse> tratamientos) {
}

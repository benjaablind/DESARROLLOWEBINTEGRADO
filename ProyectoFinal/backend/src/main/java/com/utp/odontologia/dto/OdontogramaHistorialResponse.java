package com.utp.odontologia.dto;

import java.util.List;

/**
 * Historial completo de una pieza dental (Integrante 4).
 *
 * Los registros llegan del mas reciente al mas antiguo: el primero de la lista
 * es el estado actual de la pieza.
 */
public record OdontogramaHistorialResponse(
        Long pacienteId,
        int numeroPieza,
        List<OdontogramaPiezaResponse> registros) {
}

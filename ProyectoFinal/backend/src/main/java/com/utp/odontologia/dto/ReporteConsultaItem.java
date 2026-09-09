package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.HistoriaClinica;

/** Fila resumida de una consulta de historia clinica (Integrante 6). */
public record ReporteConsultaItem(
        Long id,
        LocalDateTime fecha,
        String motivoConsulta,
        String diagnostico,
        boolean anulada) {

    /** Arma la fila a partir de la entidad del dominio. */
    public static ReporteConsultaItem desde(HistoriaClinica consulta) {
        return new ReporteConsultaItem(
                consulta.getId(),
                consulta.getFecha(),
                consulta.getMotivoConsulta(),
                consulta.getDiagnostico(),
                consulta.isAnulada());
    }
}

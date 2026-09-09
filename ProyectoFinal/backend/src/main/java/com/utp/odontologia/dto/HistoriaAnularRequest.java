package com.utp.odontologia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Motivo por el que se anula una consulta de historia clinica (Integrante 4).
 *
 * La consulta no se borra: queda marcada como anulada y sigue apareciendo en el
 * historial del paciente junto con la razon de la anulacion.
 */
public record HistoriaAnularRequest(

        @NotBlank(message = "El motivo de anulacion es obligatorio")
        String motivo) {
}

package com.utp.odontologia.dto;

import java.util.List;

/**
 * Odontograma actual de un paciente (Integrante 4).
 *
 * Contiene el ultimo estado registrado de cada pieza, ordenado por numero de
 * pieza segun la notacion FDI.
 *
 * @param totalRegistros cantidad de registros historicos del paciente, que es
 *                       mayor o igual a la cantidad de piezas mostradas
 */
public record OdontogramaResponse(
        Long pacienteId,
        String pacienteNombre,
        List<OdontogramaPiezaResponse> piezas,
        int totalRegistros) {
}

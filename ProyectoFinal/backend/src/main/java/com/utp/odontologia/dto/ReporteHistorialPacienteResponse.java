package com.utp.odontologia.dto;

import java.util.List;

/**
 * Historial consolidado de un paciente (Integrante 6).
 * Reune en una sola respuesta todo lo que los demas modulos registraron sobre
 * el paciente, para imprimirlo o exportarlo sin encadenar varias peticiones.
 */
public record ReporteHistorialPacienteResponse(
        ReportePacienteItem paciente,
        long totalConsultas,
        long totalCitas,
        long totalTratamientos,
        long totalArchivos,
        double totalPagado,
        double saldoPendiente,
        List<ReporteConsultaItem> consultas,
        List<ReporteCitaItem> citas,
        List<ReporteTratamientoItem> tratamientos,
        List<ArchivoResponse> archivos) {
}

package com.utp.productosapi.dto;

import java.time.LocalDateTime;

/**
 * Reto de aplicacion (seccion 15.1): respuesta de GET /api/admin/reporte.
 */
public record ReporteResponse(
        LocalDateTime fechaServidor,
        long totalProductos) {
}

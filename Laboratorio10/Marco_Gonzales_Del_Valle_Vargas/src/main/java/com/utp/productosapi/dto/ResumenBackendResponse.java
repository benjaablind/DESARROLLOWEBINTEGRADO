package com.utp.productosapi.dto;

/**
 * Semana 10 (guia, seccion 8.1): respuesta de GET /api/admin/resumen.
 * Todo sale de MySQL: no hay valores calculados en el cliente.
 */
public record ResumenBackendResponse(
        long totalProductos,
        long productosConStockBajo,
        long totalMovimientos,
        String estado) {
}

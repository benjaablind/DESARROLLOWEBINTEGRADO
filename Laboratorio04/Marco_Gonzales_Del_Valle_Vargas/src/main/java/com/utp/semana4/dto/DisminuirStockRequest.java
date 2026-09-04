package com.utp.semana4.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para PATCH /api/productos/{id}/disminuir-stock (ejercicio complementario 2).
 * Indica cuantas unidades se descuentan del stock actual.
 */
public class DisminuirStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

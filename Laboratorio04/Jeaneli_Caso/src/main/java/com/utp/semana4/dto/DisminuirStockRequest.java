package com.utp.semana4.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Usado por el Ejercicio 2 (seccion 28 de la guia): disminuir stock.
public class DisminuirStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad a disminuir debe ser mayor a cero")
    private Integer cantidad;

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}

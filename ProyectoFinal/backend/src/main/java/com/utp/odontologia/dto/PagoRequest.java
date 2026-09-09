package com.utp.odontologia.dto;

import com.utp.odontologia.model.MetodoPago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para registrar un pago (Integrante 5).
 * El paciente no se envia: se toma del tratamiento al que pertenece el pago.
 */
public record PagoRequest(

        @NotNull(message = "El tratamiento es obligatorio")
        Long tratamientoId,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        Double monto,

        @NotNull(message = "El metodo de pago es obligatorio")
        MetodoPago metodo,

        String comprobante,

        String observaciones) {
}

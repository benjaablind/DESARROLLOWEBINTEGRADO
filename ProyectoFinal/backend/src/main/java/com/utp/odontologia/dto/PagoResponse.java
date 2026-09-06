package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.MetodoPago;
import com.utp.odontologia.model.Pago;

/**
 * Vista de un pago para el cliente (Integrante 5).
 * Incluye el nombre del tratamiento y del paciente ya resueltos.
 */
public record PagoResponse(
        Long id,
        Long tratamientoId,
        String tratamientoNombre,
        Long pacienteId,
        String pacienteNombre,
        double monto,
        MetodoPago metodo,
        LocalDateTime fecha,
        String comprobante,
        String observaciones) {

    /** Convierte el modelo interno en la respuesta que ve el cliente. */
    public static PagoResponse desde(Pago pago, String tratamientoNombre, String pacienteNombre) {
        return new PagoResponse(
                pago.getId(),
                pago.getTratamientoId(),
                tratamientoNombre == null ? Textos.SIN_DATO : tratamientoNombre,
                pago.getPacienteId(),
                pacienteNombre == null ? Textos.SIN_DATO : pacienteNombre,
                pago.getMonto(),
                pago.getMetodo(),
                pago.getFecha(),
                pago.getComprobante(),
                pago.getObservaciones());
    }
}

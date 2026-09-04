package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoPieza;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar el estado de una pieza dental (Integrante 4).
 *
 * Cada solicitud crea un registro nuevo en el odontograma: nunca modifica los
 * registros anteriores de la pieza.
 *
 * Las anotaciones @Min y @Max solo acotan el rango general de la notacion FDI.
 * La validacion fina de los cuadrantes vive en OdontogramaService.
 */
public record OdontogramaRegistroRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El odontologo es obligatorio")
        Long odontologoId,

        @NotNull(message = "El numero de pieza es obligatorio")
        @Min(value = 11, message = "El numero de pieza debe estar entre 11 y 85")
        @Max(value = 85, message = "El numero de pieza debe estar entre 11 y 85")
        Integer numeroPieza,

        @NotNull(message = "El estado de la pieza es obligatorio")
        EstadoPieza estado,

        String superficie,

        String diagnostico,

        String tratamiento,

        String observaciones) {
}

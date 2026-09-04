package com.utp.odontologia.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para agregar una sesion a un tratamiento (Integrante 5).
 * El numero de sesion lo calcula el servicio, no lo envia el cliente.
 */
public record SesionRequest(

        @NotNull(message = "La fecha de la sesion es obligatoria")
        LocalDate fecha,

        @NotBlank(message = "La descripcion de la sesion es obligatoria")
        String descripcion,

        String observaciones) {
}

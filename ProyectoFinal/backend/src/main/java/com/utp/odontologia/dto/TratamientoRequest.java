package com.utp.odontologia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Datos de entrada para crear o actualizar un tratamiento (Integrante 5).
 * El estado no se envia aqui: se maneja con PATCH /api/tratamientos/{id}/estado.
 */
public record TratamientoRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El odontologo es obligatorio")
        Long odontologoId,

        @NotBlank(message = "El nombre del tratamiento es obligatorio")
        String nombre,

        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a cero")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal precio,

        LocalDate fechaInicio,

        String observaciones) {
}

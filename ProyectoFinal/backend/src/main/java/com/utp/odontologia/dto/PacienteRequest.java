package com.utp.odontologia.dto;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

/**
 * Datos de entrada para crear o actualizar un paciente (Integrante 2).
 * Las validaciones se declaran aqui para que el controlador no las repita.
 */
public record PacienteRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
        String dni,

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El sexo es obligatorio")
        @Pattern(regexp = "M|F", message = "El sexo debe ser M o F")
        String sexo,

        @NotBlank(message = "El telefono es obligatorio")
        String telefono,

        @Email(message = "El email no tiene un formato valido")
        String email,

        String direccion,

        String distrito,

        String ciudad,

        @Valid
        AntecedentesRequest antecedentes) {
}

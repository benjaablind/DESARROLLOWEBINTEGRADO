package com.utp.odontologia.dto;

import com.utp.odontologia.model.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos editables de un usuario ya registrado (Integrante 1).
 * El nombre de usuario no se modifica porque identifica al usuario en el login,
 * y la contrasena tiene su propio endpoint.
 */
public record ActualizarUsuarioRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}

package com.utp.odontologia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales que envia el formulario de inicio de sesion (Integrante 1).
 */
public record LoginRequest(

        @NotBlank(message = "El usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La contrasena es obligatoria")
        String password) {
}

package com.utp.odontologia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de cambio de contrasena (Integrante 1).
 * Se pide la contrasena actual para confirmar que quien la cambia es el dueno de la cuenta.
 */
public record CambiarPasswordRequest(

        @NotBlank(message = "La contrasena actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "La contrasena nueva es obligatoria")
        @Size(min = 6, message = "La contrasena nueva debe tener al menos 6 caracteres")
        String passwordNuevo) {
}

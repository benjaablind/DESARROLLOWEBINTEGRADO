package com.utp.odontologia.dto;

import com.utp.odontologia.model.Rol;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que envia el cliente para registrar un usuario nuevo (Integrante 1).
 * Se valida con @Valid en el controlador; los errores los traduce ApiExceptionHandler a 400.
 */
public record UsuarioRequest(

        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 4, max = 30, message = "El nombre de usuario debe tener entre 4 y 30 caracteres")
        String usuario,

        // La contrasena viaja en texto plano porque en esta etapa no hay cifrado.
        // BCrypt y Spring Security entran en las semanas 6 a 10 del curso.
        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol) {
}

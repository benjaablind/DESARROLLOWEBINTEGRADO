package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;

/**
 * Vista publica de un usuario (Integrante 1).
 * Nunca expone la contrasena: es el DTO que devuelven todos los endpoints.
 */
public record UsuarioResponse(
        Long id,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String email,
        String usuario,
        Rol rol,
        boolean activo,
        LocalDateTime fechaRegistro) {

    /** Convierte el modelo interno en la respuesta que ve el cliente. */
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getUsuario(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaRegistro());
    }
}

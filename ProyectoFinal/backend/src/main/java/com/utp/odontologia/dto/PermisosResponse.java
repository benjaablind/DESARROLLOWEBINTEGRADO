package com.utp.odontologia.dto;

import java.util.List;

import com.utp.odontologia.model.Rol;

/**
 * Permisos asociados a un rol (Integrante 1).
 *
 * El frontend los usa para mostrar u ocultar opciones del menu. La verificacion
 * real en el servidor llegara con Spring Security en las semanas 6 a 10.
 */
public record PermisosResponse(
        Rol rol,
        List<String> permisos) {
}

package com.utp.odontologia.dto;

import com.utp.odontologia.model.Usuario;

/**
 * Datos minimos de un odontologo para los selectores del frontend.
 *
 * GET /api/usuarios es solo del administrador porque expone email, usuario y
 * rol. Recepcion y los odontologos necesitan elegir al odontologo de una cita,
 * un tratamiento o una consulta, y para eso basta con el id y el nombre.
 */
public record OdontologoResponse(Long id, String nombreCompleto) {

    public static OdontologoResponse desde(Usuario usuario) {
        return new OdontologoResponse(usuario.getId(), usuario.getNombreCompleto());
    }
}

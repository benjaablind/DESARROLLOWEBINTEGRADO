package com.utp.odontologia.dto;

/**
 * Resultado de un inicio de sesion correcto (Integrante 1).
 *
 * Todavia no incluye token: mientras no exista JWT (semanas 6 a 10) el frontend
 * guarda el usuario devuelto y envia su id en las llamadas siguientes.
 */
public record LoginResponse(
        boolean autenticado,
        UsuarioResponse usuario,
        String mensaje) {

    /** Atajo para construir la respuesta de una autenticacion exitosa. */
    public static LoginResponse exitoso(UsuarioResponse usuario) {
        return new LoginResponse(true, usuario, "Autenticacion correcta");
    }
}

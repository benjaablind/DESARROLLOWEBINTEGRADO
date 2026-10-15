package com.utp.odontologia.dto;

/**
 * Respuesta del inicio de sesion (semana 9).
 *
 * Hasta la semana 5 solo devolvia los datos del usuario y el servidor no
 * recordaba nada. Ahora incluye el JWT: el cliente lo envia en cada peticion
 * como "Authorization: Bearer <accessToken>" hasta que venza.
 *
 * Nunca incluye la contrasena ni su hash.
 */
public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        UsuarioResponse usuario) {

    public static LoginResponse bearer(String token, long expiresInSeconds, UsuarioResponse usuario) {
        return new LoginResponse("Bearer", token, expiresInSeconds, usuario);
    }
}

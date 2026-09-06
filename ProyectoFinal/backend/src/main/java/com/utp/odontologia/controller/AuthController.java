package com.utp.odontologia.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.LoginRequest;
import com.utp.odontologia.dto.LoginResponse;
import com.utp.odontologia.dto.UsuarioResponse;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * API REST de autenticacion (Integrante 1).
 *
 * En esta etapa el login solo compara las credenciales guardadas en memoria.
 * No hay tokens ni sesiones: Spring Security y JWT entran en las semanas 6 a 10
 * del curso y reemplazaran el contenido de esta clase sin cambiar las rutas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * POST /api/auth/login
     * Devuelve 200 con los datos del usuario, o 400 si las credenciales son
     * invalidas o el usuario esta inactivo (lo traduce ApiExceptionHandler).
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = usuarioService.autenticar(request);
        return ResponseEntity.ok(LoginResponse.exitoso(UsuarioResponse.desde(usuario)));
    }

    /**
     * POST /api/auth/logout
     *
     * Sin JWT no hay sesion que invalidar en el servidor: el cierre de sesion se
     * resuelve en el frontend borrando el usuario guardado. Este endpoint existe
     * para dejar el contrato listo y se completara en la etapa de seguridad
     * (semanas 6 a 10), cuando haya un token que revocar.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensaje", "Sesion cerrada correctamente"));
    }

    /**
     * GET /api/auth/perfil?usuarioId=
     *
     * Devuelve el perfil del usuario indicado. Mientras no exista JWT el
     * frontend envia el id porque el servidor no sabe quien es el solicitante.
     */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> perfil(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.buscarPorId(usuarioId)));
    }
}

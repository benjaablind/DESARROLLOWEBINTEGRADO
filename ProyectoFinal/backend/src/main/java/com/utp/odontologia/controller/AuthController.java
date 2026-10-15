package com.utp.odontologia.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.LoginRequest;
import com.utp.odontologia.dto.LoginResponse;
import com.utp.odontologia.dto.UsuarioResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.security.TokenService;
import com.utp.odontologia.security.UsuarioActual;
import com.utp.odontologia.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * API REST de autenticacion (Integrante 1).
 *
 * Semanas 8 y 9: el login valida usuario y contrasena (BCrypt) una sola vez y
 * emite un JWT. Las rutas son las mismas de la semana 5; cambia lo que hay
 * detras de ellas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioActual usuarioActual;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,
            UsuarioActual usuarioActual, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.usuarioActual = usuarioActual;
        this.usuarioService = usuarioService;
    }

    /**
     * POST /api/auth/login
     *
     * 200 con el token y los datos del usuario. Las credenciales invalidas
     * responden 401 y un usuario inactivo 403; las dos excepciones las traduce
     * ApiExceptionHandler. El mensaje es el mismo para usuario inexistente y
     * contrasena incorrecta, para no revelar que nombres de usuario existen.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.password()));

        String token = tokenService.crearToken(autenticacion);
        Usuario usuario = usuarioService.buscarPorNombreDeUsuario(autenticacion.getName());

        return ResponseEntity.ok(LoginResponse.bearer(token, tokenService.expiresInSeconds(),
                UsuarioResponse.desde(usuario)));
    }

    /**
     * POST /api/auth/logout
     *
     * Un JWT no se guarda en el servidor, asi que no hay nada que borrar aqui:
     * el cliente debe descartar su token. El token sigue siendo valido hasta su
     * vencimiento (app.jwt.ttl-minutes); revocarlo antes requeriria una lista de
     * tokens anulados, fuera del alcance de esta etapa.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("mensaje",
                "Sesion cerrada: el cliente debe descartar su token"));
    }

    /**
     * GET /api/auth/perfil
     *
     * Devuelve el perfil del usuario dueno del token. Hasta la semana 5 el
     * cliente enviaba ?usuarioId= porque el servidor no sabia quien llamaba;
     * ese parametro ya no se usa.
     */
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> perfil() {
        Usuario usuario = usuarioActual.obtener()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario del token ya no existe"));
        return ResponseEntity.ok(UsuarioResponse.desde(usuario));
    }
}

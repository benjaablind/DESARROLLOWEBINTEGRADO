package com.utp.odontologia.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utp.odontologia.dto.ActualizarUsuarioRequest;
import com.utp.odontologia.dto.CambiarPasswordRequest;
import com.utp.odontologia.dto.PermisosResponse;
import com.utp.odontologia.dto.UsuarioRequest;
import com.utp.odontologia.dto.UsuarioResponse;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.service.UsuarioService;

import jakarta.validation.Valid;

/**
 * API REST de usuarios (Integrante 1).
 *
 * Solo traduce HTTP: delega las reglas de negocio en UsuarioService y los
 * errores en ApiExceptionHandler. Nunca devuelve el modelo Usuario directamente,
 * siempre UsuarioResponse, que no expone la contrasena.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** GET /api/usuarios?rol=&activo= : los dos filtros son opcionales. */
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean activo) {

        List<UsuarioResponse> respuesta = usuarioService.listar(rol, activo).stream()
                .map(UsuarioResponse::desde)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    /** GET /api/usuarios/{id} : 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.buscarPorId(id)));
    }

    /** POST /api/usuarios : 201 con la cabecera Location del recurso creado. */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario creado = usuarioService.crear(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(UsuarioResponse.desde(creado));
    }

    /** PUT /api/usuarios/{id} : actualiza datos personales y rol. */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.actualizar(id, request)));
    }

    /** PATCH /api/usuarios/{id}/password : requiere la contrasena actual. */
    @PatchMapping("/{id}/password")
    public ResponseEntity<UsuarioResponse> cambiarPassword(@PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request) {
        return ResponseEntity.ok(
                UsuarioResponse.desde(usuarioService.cambiarPassword(id, request)));
    }

    /** PATCH /api/usuarios/{id}/activar */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.activar(id)));
    }

    /** PATCH /api/usuarios/{id}/desactivar : baja logica, el usuario no se borra. */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.desde(usuarioService.desactivar(id)));
    }

    /** DELETE /api/usuarios/{id} : 204 sin cuerpo. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/usuarios/{id}/permisos : permisos que corresponden al rol del usuario. */
    @GetMapping("/{id}/permisos")
    public ResponseEntity<PermisosResponse> permisos(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(new PermisosResponse(
                usuario.getRol(),
                usuarioService.permisosDe(usuario.getRol())));
    }
}

package com.utp.odontologia.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.utp.odontologia.dto.SesionEstadoRequest;
import com.utp.odontologia.dto.SesionRequest;
import com.utp.odontologia.dto.TratamientoEstadoRequest;
import com.utp.odontologia.dto.TratamientoRequest;
import com.utp.odontologia.dto.TratamientoResponse;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.service.TratamientoService;

import jakarta.validation.Valid;

/**
 * API REST de tratamientos y de sus sesiones (Integrante 5).
 * Solo traduce HTTP: delega las reglas en TratamientoService y deja que
 * ApiExceptionHandler convierta las excepciones en respuestas de error.
 */
@RestController
@RequestMapping("/api/tratamientos")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    /** GET /api/tratamientos?pacienteId=&estado=&soloActivos= - listado con filtros. */
    @GetMapping
    public ResponseEntity<List<TratamientoResponse>> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) EstadoTratamiento estado,
            @RequestParam(required = false) Boolean soloActivos) {

        return ResponseEntity.ok(tratamientoService.listar(pacienteId, estado, soloActivos));
    }

    /** GET /api/tratamientos/{id} - detalle con sesiones y resumen economico. */
    @GetMapping("/{id}")
    public ResponseEntity<TratamientoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tratamientoService.detalle(id));
    }

    /** POST /api/tratamientos - registra un tratamiento y devuelve 201 con Location. */
    @PostMapping
    public ResponseEntity<TratamientoResponse> crear(
            @Valid @RequestBody TratamientoRequest request) {

        TratamientoResponse creado = tratamientoService.crear(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** PUT /api/tratamientos/{id} - reemplaza los datos de un tratamiento abierto. */
    @PutMapping("/{id}")
    public ResponseEntity<TratamientoResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody TratamientoRequest request) {

        return ResponseEntity.ok(tratamientoService.actualizar(id, request));
    }

    /** PATCH /api/tratamientos/{id}/estado - avanza el tratamiento de estado. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TratamientoResponse> cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody TratamientoEstadoRequest request) {

        return ResponseEntity.ok(tratamientoService.cambiarEstado(id, request.estado()));
    }

    /** POST /api/tratamientos/{id}/sesiones - agrega una sesion al tratamiento. */
    @PostMapping("/{id}/sesiones")
    public ResponseEntity<TratamientoResponse> agregarSesion(@PathVariable Long id,
            @Valid @RequestBody SesionRequest request) {

        TratamientoResponse actualizado = tratamientoService.agregarSesion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(actualizado);
    }

    /** PATCH /api/tratamientos/{id}/sesiones/{sesionId} - marca la sesion realizada. */
    @PatchMapping("/{id}/sesiones/{sesionId}")
    public ResponseEntity<TratamientoResponse> actualizarSesion(@PathVariable Long id,
            @PathVariable Long sesionId,
            @Valid @RequestBody SesionEstadoRequest request) {

        return ResponseEntity.ok(tratamientoService.actualizarSesion(id, sesionId, request));
    }

    /** DELETE /api/tratamientos/{id}/sesiones/{sesionId} - quita una sesion. */
    @DeleteMapping("/{id}/sesiones/{sesionId}")
    public ResponseEntity<Void> eliminarSesion(@PathVariable Long id,
            @PathVariable Long sesionId) {

        tratamientoService.eliminarSesion(id, sesionId);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/tratamientos/{id} - elimina el tratamiento si no tiene pagos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tratamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

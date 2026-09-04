package com.utp.odontologia.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.utp.odontologia.dto.CitaEstadoRequest;
import com.utp.odontologia.dto.CitaReprogramarRequest;
import com.utp.odontologia.dto.CitaRequest;
import com.utp.odontologia.dto.CitaResponse;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.service.CitaService;

import jakarta.validation.Valid;

/**
 * API REST de citas y agenda (Integrante 3).
 * Solo traduce HTTP: las reglas de negocio viven en CitaService y los errores
 * los convierte ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /** GET /api/citas con filtros opcionales y combinables. */
    @GetMapping
    public ResponseEntity<List<CitaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long odontologoId,
            @RequestParam(required = false) EstadoCita estado) {

        return ResponseEntity.ok(citaService.listar(dia, pacienteId, odontologoId, estado));
    }

    /** GET /api/citas/agenda: agenda completa de un dia. */
    @GetMapping("/agenda")
    public ResponseEntity<List<CitaResponse>> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        return ResponseEntity.ok(citaService.agendaDelDia(dia));
    }

    /** GET /api/citas/disponibilidad: horas ocupadas de un odontologo en un dia. */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<String>> disponibilidad(
            @RequestParam Long odontologoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dia) {

        return ResponseEntity.ok(citaService.disponibilidad(odontologoId, dia));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.buscarPorId(id));
    }

    /** POST /api/citas: crea la cita y devuelve 201 con la cabecera Location. */
    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest peticion) {
        CitaResponse creada = citaService.crear(peticion);
        return ResponseEntity.created(URI.create("/api/citas/" + creada.id())).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody CitaRequest peticion) {
        return ResponseEntity.ok(citaService.actualizar(id, peticion));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody CitaEstadoRequest peticion) {
        return ResponseEntity.ok(citaService.cambiarEstado(id, peticion));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<CitaResponse> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.confirmar(id));
    }

    /** PATCH /api/citas/{id}/asistencia: cierra la cita como atendida o no asistio. */
    @PatchMapping("/{id}/asistencia")
    public ResponseEntity<CitaResponse> registrarAsistencia(@PathVariable Long id,
            @RequestParam boolean asistio) {
        return ResponseEntity.ok(citaService.registrarAsistencia(id, asistio));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelar(@PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(citaService.cancelar(id, motivo));
    }

    /** POST /api/citas/{id}/reprogramar: devuelve 201 apuntando a la cita nueva. */
    @PostMapping("/{id}/reprogramar")
    public ResponseEntity<CitaResponse> reprogramar(@PathVariable Long id,
            @Valid @RequestBody CitaReprogramarRequest peticion) {

        CitaResponse nueva = citaService.reprogramar(id, peticion);
        return ResponseEntity.created(URI.create("/api/citas/" + nueva.id())).body(nueva);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

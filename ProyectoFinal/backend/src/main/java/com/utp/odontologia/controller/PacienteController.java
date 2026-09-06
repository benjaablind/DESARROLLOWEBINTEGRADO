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

import com.utp.odontologia.dto.AntecedentesRequest;
import com.utp.odontologia.dto.PacienteEstadoRequest;
import com.utp.odontologia.dto.PacienteRequest;
import com.utp.odontologia.dto.PacienteResponse;
import com.utp.odontologia.dto.PacienteResumenResponse;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.service.PacienteService;

import jakarta.validation.Valid;

/**
 * API REST de pacientes (Integrante 2).
 * Solo traduce HTTP: delega las reglas de negocio en PacienteService y deja
 * que ApiExceptionHandler convierta las excepciones en respuestas de error.
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /** GET /api/pacientes?texto=&estado= - listado resumido con filtros opcionales. */
    @GetMapping
    public ResponseEntity<List<PacienteResumenResponse>> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoPaciente estado) {

        List<PacienteResumenResponse> respuesta = pacienteService.listar(texto, estado)
                .stream()
                .map(PacienteResumenResponse::desde)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    /** GET /api/pacientes/{id} - ficha completa del paciente. */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PacienteResponse.desde(pacienteService.buscarPorId(id)));
    }

    /** GET /api/pacientes/dni/{dni} - busqueda directa por documento. */
    @GetMapping("/dni/{dni}")
    public ResponseEntity<PacienteResponse> buscarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(PacienteResponse.desde(pacienteService.buscarPorDni(dni)));
    }

    /** POST /api/pacientes - registra un paciente y devuelve 201 con Location. */
    @PostMapping
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest request) {
        Paciente creado = pacienteService.crear(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(PacienteResponse.desde(creado));
    }

    /** PUT /api/pacientes/{id} - reemplaza los datos del paciente. */
    @PutMapping("/{id}")
    public ResponseEntity<PacienteResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody PacienteRequest request) {
        return ResponseEntity.ok(PacienteResponse.desde(pacienteService.actualizar(id, request)));
    }

    /** PATCH /api/pacientes/{id}/antecedentes - actualiza solo los antecedentes. */
    @PatchMapping("/{id}/antecedentes")
    public ResponseEntity<PacienteResponse> actualizarAntecedentes(@PathVariable Long id,
            @Valid @RequestBody AntecedentesRequest request) {
        return ResponseEntity.ok(
                PacienteResponse.desde(pacienteService.actualizarAntecedentes(id, request)));
    }

    /** PATCH /api/pacientes/{id}/estado - activa o desactiva al paciente. */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PacienteResponse> cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody PacienteEstadoRequest request) {
        return ResponseEntity.ok(
                PacienteResponse.desde(pacienteService.cambiarEstado(id, request.estado())));
    }

    /** DELETE /api/pacientes/{id} - elimina al paciente. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package com.utp.odontologia.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.HistoriaAnularRequest;
import com.utp.odontologia.dto.HistoriaClinicaRequest;
import com.utp.odontologia.dto.HistoriaClinicaResponse;
import com.utp.odontologia.service.HistoriaClinicaService;

import jakarta.validation.Valid;

/**
 * API REST de la historia clinica (Integrante 4).
 *
 * Solo traduce HTTP: las reglas de negocio viven en HistoriaClinicaService y los
 * errores los transforma ApiExceptionHandler.
 */
@RestController
@RequestMapping("/api/historias")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    /** Historial de un paciente, de la consulta mas reciente a la mas antigua. */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaClinicaResponse>> listarPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaClinicaService.listarPorPaciente(pacienteId));
    }

    /** Detalle de una consulta. */
    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinicaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historiaClinicaService.buscarPorId(id));
    }

    /** Registra una consulta nueva y devuelve 201 con la cabecera Location. */
    @PostMapping
    public ResponseEntity<HistoriaClinicaResponse> crear(
            @Valid @RequestBody HistoriaClinicaRequest request) {

        HistoriaClinicaResponse creada = historiaClinicaService.crear(request);
        URI ubicacion = URI.create("/api/historias/" + creada.id());

        return ResponseEntity.created(ubicacion).body(creada);
    }

    /** Anula una consulta registrada por error, dejando constancia del motivo. */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<HistoriaClinicaResponse> anular(@PathVariable Long id,
            @Valid @RequestBody HistoriaAnularRequest request) {
        return ResponseEntity.ok(historiaClinicaService.anular(id, request));
    }

    // Este controlador no expone PUT ni DELETE de forma deliberada.
    // La historia clinica es un documento legal: editar una consulta ya
    // registrada falsearia el expediente y borrarla dejaria un hueco imposible
    // de auditar. La unica correccion admitida es PATCH /{id}/anular, que marca
    // la consulta como anulada sin quitarla del historial del paciente.
}

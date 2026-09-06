package com.utp.odontologia.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utp.odontologia.dto.EstadoCuentaResponse;
import com.utp.odontologia.dto.PagoRequest;
import com.utp.odontologia.dto.PagoResponse;
import com.utp.odontologia.service.PagoService;

import jakarta.validation.Valid;

/**
 * API REST de pagos y estado de cuenta (Integrante 5).
 * Solo traduce HTTP: las validaciones de saldo viven en PagoService.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    /** GET /api/pagos?pacienteId=&tratamientoId= - listado con filtros opcionales. */
    @GetMapping
    public ResponseEntity<List<PagoResponse>> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long tratamientoId) {

        return ResponseEntity.ok(pagoService.listar(pacienteId, tratamientoId));
    }

    /** GET /api/pagos/{id} - detalle de un pago. */
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.detalle(id));
    }

    /** GET /api/pagos/tratamiento/{tratamientoId} - historial de pagos por fecha. */
    @GetMapping("/tratamiento/{tratamientoId}")
    public ResponseEntity<List<PagoResponse>> historialDeTratamiento(
            @PathVariable Long tratamientoId) {

        return ResponseEntity.ok(pagoService.historialDeTratamiento(tratamientoId));
    }

    /** GET /api/pagos/estado-cuenta/{pacienteId} - resumen economico del paciente. */
    @GetMapping("/estado-cuenta/{pacienteId}")
    public ResponseEntity<EstadoCuentaResponse> estadoDeCuenta(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(pagoService.estadoDeCuenta(pacienteId));
    }

    /** POST /api/pagos - registra un pago y devuelve 201 con Location. */
    @PostMapping
    public ResponseEntity<PagoResponse> registrar(@Valid @RequestBody PagoRequest request) {
        PagoResponse creado = pagoService.registrar(request);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();

        return ResponseEntity.created(ubicacion).body(creado);
    }

    /** DELETE /api/pagos/{id} - anula un pago y devuelve el saldo al tratamiento. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

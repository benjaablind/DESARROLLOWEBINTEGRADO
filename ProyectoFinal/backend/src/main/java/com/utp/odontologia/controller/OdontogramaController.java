package com.utp.odontologia.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.OdontogramaHistorialResponse;
import com.utp.odontologia.dto.OdontogramaPiezaResponse;
import com.utp.odontologia.dto.OdontogramaRegistroRequest;
import com.utp.odontologia.dto.OdontogramaResponse;
import com.utp.odontologia.model.EstadoPieza;
import com.utp.odontologia.service.OdontogramaService;

import jakarta.validation.Valid;

/**
 * API REST del odontograma (Integrante 4).
 *
 * El odontograma se construye solo con altas: cada POST agrega un estado nuevo a
 * la pieza y conserva los anteriores. Por eso no hay PUT ni DELETE.
 */
@RestController
@RequestMapping("/api/odontograma")
public class OdontogramaController {

    private final OdontogramaService odontogramaService;

    public OdontogramaController(OdontogramaService odontogramaService) {
        this.odontogramaService = odontogramaService;
    }

    /** Odontograma actual: ultimo estado de cada pieza del paciente. */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<OdontogramaResponse> obtenerActual(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(odontogramaService.obtenerActual(pacienteId));
    }

    /** Historial de una pieza, del registro mas reciente al mas antiguo. */
    @GetMapping("/paciente/{pacienteId}/pieza/{numeroPieza}")
    public ResponseEntity<OdontogramaHistorialResponse> historialDePieza(
            @PathVariable Long pacienteId, @PathVariable int numeroPieza) {
        return ResponseEntity.ok(odontogramaService.historialDePieza(pacienteId, numeroPieza));
    }

    /** Cantidad de piezas en cada estado segun el odontograma actual. */
    @GetMapping("/paciente/{pacienteId}/resumen")
    public ResponseEntity<Map<EstadoPieza, Long>> resumenEstados(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(odontogramaService.resumenEstados(pacienteId));
    }

    /** Registra un estado nuevo de una pieza y devuelve 201 con Location. */
    @PostMapping
    public ResponseEntity<OdontogramaPiezaResponse> registrar(
            @Valid @RequestBody OdontogramaRegistroRequest request) {

        OdontogramaPiezaResponse creado = odontogramaService.registrar(request);
        URI ubicacion = URI.create("/api/odontograma/paciente/" + request.pacienteId()
                + "/pieza/" + creado.numeroPieza());

        return ResponseEntity.created(ubicacion).body(creado);
    }
}

package com.utp.odontologia.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.ReporteCitasResponse;
import com.utp.odontologia.dto.ReporteHistorialPacienteResponse;
import com.utp.odontologia.dto.ReporteIngresosResponse;
import com.utp.odontologia.dto.ReportePacientesResponse;
import com.utp.odontologia.dto.ReportePagosPendientesResponse;
import com.utp.odontologia.dto.ReporteTratamientosResponse;
import com.utp.odontologia.service.ReporteService;

/**
 * API REST de reportes gerenciales (Integrante 6).
 *
 * Todos los endpoints son de solo lectura. Las fechas se reciben en formato
 * ISO (yyyy-MM-dd) y son opcionales: si no se envian, el servicio aplica un
 * rango amplio por defecto.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /** GET /api/reportes/pacientes?desde=&hasta= - pacientes registrados en el periodo. */
    @GetMapping("/pacientes")
    public ResponseEntity<ReportePacientesResponse> pacientes(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(reporteService.pacientesRegistrados(desde, hasta));
    }

    /** GET /api/reportes/citas?desde=&hasta= - citas del periodo agrupadas por estado. */
    @GetMapping("/citas")
    public ResponseEntity<ReporteCitasResponse> citas(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(reporteService.citasPorPeriodo(desde, hasta));
    }

    /** GET /api/reportes/tratamientos?desde=&hasta= - realizados frente a pendientes. */
    @GetMapping("/tratamientos")
    public ResponseEntity<ReporteTratamientosResponse> tratamientos(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(reporteService.tratamientos(desde, hasta));
    }

    /** GET /api/reportes/ingresos?desde=&hasta= - cobranza por metodo de pago y por mes. */
    @GetMapping("/ingresos")
    public ResponseEntity<ReporteIngresosResponse> ingresos(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        return ResponseEntity.ok(reporteService.ingresos(desde, hasta));
    }

    /** GET /api/reportes/pagos-pendientes - deuda vigente por cobrar. */
    @GetMapping("/pagos-pendientes")
    public ResponseEntity<ReportePagosPendientesResponse> pagosPendientes() {
        return ResponseEntity.ok(reporteService.pagosPendientes());
    }

    /** GET /api/reportes/historial/{pacienteId} - consolidado clinico del paciente. */
    @GetMapping("/historial/{pacienteId}")
    public ResponseEntity<ReporteHistorialPacienteResponse> historial(
            @PathVariable Long pacienteId) {

        return ResponseEntity.ok(reporteService.historialDePaciente(pacienteId));
    }
}
